package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class PlayerWatchDistanceMixin implements NativePlayerWatchDistanceAccess {
    @Shadow private int watchDistance;
    @Shadow @Final private PlayerChunkWatchingManager playerChunkWatchingManager;
    @Shadow @Final private Int2ObjectMap<?> entityTrackers;
    @Shadow public abstract net.minecraft.server.world.ChunkTicketManager getTicketManager();
    @Shadow protected abstract void sendWatchPackets(ServerPlayerEntity player, ChunkPos pos, MutableObject<ChunkDataS2CPacket> packet, boolean oldWithin, boolean newWithin);
    @Shadow private static boolean isOnDistanceEdge(int x, int z, int centerX, int centerZ, int distance) { throw new AssertionError(); }
    @Unique private int meldrite$watchDepth;
    @Unique private int meldrite$worldWatchDistance;
    @WrapMethod(method = "handlePlayerAddedOrRemoved")
    private void meldrite$playerJoinRadius(ServerPlayerEntity player, boolean added, Operation<Void> original) {
        meldrite$withPlayerRadius(player, () -> original.call(player, added));
    }
    @WrapMethod(method = "updatePosition")
    private void meldrite$playerWatchRadius(ServerPlayerEntity player, Operation<Void> original) {
        meldrite$withPlayerRadius(player, () -> original.call(player));
    }
    @Unique private void meldrite$withPlayerRadius(ServerPlayerEntity player, Runnable original) {
        // Native player tracking runs on the server thread. Scope the radius across the
        // complete delegated implementation, including helpers introduced by other mixins.
        // Unset players at the outermost call retain the untouched native path.
        if (meldrite$watchDepth == 0 && ((PaperPlayerAccess) player).meldrite$playerState().viewDistance < 0) {
            original.run(); return;
        }
        int previous = watchDistance;
        if (meldrite$watchDepth++ == 0) meldrite$worldWatchDistance = previous;
        watchDistance = NativePlayerDistances.view(player, meldrite$worldWatchDistance);
        try { original.run(); }
        finally { watchDistance = previous; --meldrite$watchDepth; }
    }
    @Override public int meldrite$worldWatchDistance(int current) {
        return meldrite$watchDepth == 0 ? current : meldrite$worldWatchDistance;
    }
    @Inject(method = "getPlayersWatchingChunk(Lnet/minecraft/util/math/ChunkPos;Z)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void meldrite$watchers(ChunkPos chunk, boolean edgeOnly, CallbackInfoReturnable<List<ServerPlayerEntity>> ci) {
        var candidates = playerChunkWatchingManager.getPlayersWatchingChunk(chunk.toLong());
        if (candidates.stream().noneMatch(player -> ((PaperPlayerAccess) player).meldrite$playerState().viewDistance >= 0)) return;
        var result = new ArrayList<ServerPlayerEntity>();
        for (var player : candidates) {
            var center = player.getWatchedSection(); int radius = NativePlayerDistances.view(player, meldrite$worldWatchDistance(watchDistance));
            boolean within = edgeOnly ? isOnDistanceEdge(chunk.x, chunk.z, center.getSectionX(), center.getSectionZ(), radius)
                    : ThreadedAnvilChunkStorage.isWithinDistance(chunk.x, chunk.z, center.getSectionX(), center.getSectionZ(), radius);
            if (within) result.add(player);
        }
        ci.setReturnValue(List.copyOf(result));
    }
    @Inject(method = "method_17219", at = @At("HEAD"), cancellable = true)
    private void meldrite$keepOverrideOnWorldChange(ChunkPos chunk, int previousDistance, MutableObject<ChunkDataS2CPacket> packet, ServerPlayerEntity player, CallbackInfo ci) {
        if (((PaperPlayerAccess) player).meldrite$playerState().viewDistance >= 0) ci.cancel();
    }
    @Override public void meldrite$refreshWatchDistance(ServerPlayerEntity player, int previousDistance) {
        ((NativePlayerDistanceAccess) getTicketManager()).meldrite$refreshPlayerDistances();
        int distance = NativePlayerDistances.view(player, meldrite$worldWatchDistance(watchDistance));
        var center = player.getWatchedSection(); int max = Math.max(previousDistance, distance) + 1;
        for (int x = center.getSectionX() - max; x <= center.getSectionX() + max; x++) {
            for (int z = center.getSectionZ() - max; z <= center.getSectionZ() + max; z++) {
                boolean before = ThreadedAnvilChunkStorage.isWithinDistance(x, z, center.getSectionX(), center.getSectionZ(), previousDistance);
                boolean after = ThreadedAnvilChunkStorage.isWithinDistance(x, z, center.getSectionX(), center.getSectionZ(), distance);
                if (before != after) sendWatchPackets(player, new ChunkPos(x, z), new MutableObject<>(), before, after);
            }
        }
        for (var tracker : entityTrackers.values()) if (tracker instanceof NativeEntityTrackerAccess access) access.updateTrackedStatus(player);
    }
}
