package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.SimulationDistanceLevelPropagator;
import org.spongepowered.asm.mixin.*;

/** Per-player coverage uses the existing native loading and simulation propagators. */
@Mixin(ChunkTicketManager.class)
public abstract class PlayerDistanceTicketMixin implements NativePlayerDistanceAccess {
    @Shadow @Final private Long2ObjectMap<ObjectSet<ServerPlayerEntity>> playersByChunkPos;
    @Shadow @Final private SimulationDistanceLevelPropagator simulationDistanceTracker;
    @Shadow private int simulationDistance;
    @Unique private NativeNearbyPlayerSource meldrite$nearby;
    @Unique private int meldrite$worldViewDistance;
    @Unique private boolean meldrite$weightedView;
    @Unique private boolean meldrite$weightedSimulation;
    @Unique private LongOpenHashSet meldrite$viewCenters = new LongOpenHashSet();
    @Unique private Long2IntOpenHashMap meldrite$simulationLevels = new Long2IntOpenHashMap();
    @Override public void meldrite$bindNearbySource(NativeNearbyPlayerSource source) { meldrite$nearby = source; }
    @Override public boolean meldrite$weightedViewSources() { return meldrite$weightedView; }
    @Override public int meldrite$viewSourceLevel(long chunk) {
        var players = playersByChunkPos.get(chunk);
        if (players == null || players.isEmpty()) return Integer.MAX_VALUE;
        int radius = 0;
        for (var player : players) radius = Math.max(radius, NativePlayerDistances.view(player, meldrite$worldViewDistance));
        return 32 - radius;
    }
    @WrapMethod(method = "setWatchDistance")
    private void meldrite$worldView(int radius, Operation<Void> original) {
        // The merged implementation owns its loading sources; preserve all of its work.
        original.call(radius);
        meldrite$worldViewDistance = radius;
        if (meldrite$weightedView) {
            meldrite$nearby.meldrite$setRadius(32);
            meldrite$refreshPlayerDistances();
        }
    }
    @WrapMethod(method = "handleChunkEnter")
    private void meldrite$enterSource(ChunkSectionPos section, ServerPlayerEntity player, Operation<Void> original) {
        original.call(section, player);
        meldrite$moveSource(player);
    }
    @WrapMethod(method = "handleChunkLeave")
    private void meldrite$leaveSource(ChunkSectionPos section, ServerPlayerEntity player, Operation<Void> original) {
        original.call(section, player);
        meldrite$moveSource(player);
    }
    @Unique private void meldrite$moveSource(ServerPlayerEntity player) {
        var state = ((PaperPlayerAccess) player).meldrite$playerState();
        if (meldrite$weightedView || meldrite$weightedSimulation || state.viewDistance >= 0 || state.simulationDistance >= 0) meldrite$refreshPlayerDistances();
    }
    @WrapMethod(method = "setSimulationDistance")
    private void meldrite$worldSimulation(int distance, Operation<Void> original) {
        int previous = simulationDistance;
        original.call(distance);
        if (meldrite$weightedSimulation && previous != simulationDistance) {
            // Vanilla relevels all PLAYER tickets. Their current level is now the new world default.
            meldrite$simulationLevels.clear();
            for (long chunk : playersByChunkPos.keySet()) meldrite$simulationLevels.put(chunk, meldrite$simulationLevel(simulationDistance));
            meldrite$refreshPlayerDistances();
        }
    }
    @Override public void meldrite$refreshPlayerDistances() {
        boolean view = false, simulation = false;
        for (var players : playersByChunkPos.values()) for (var player : players) {
            var state = ((PaperPlayerAccess) player).meldrite$playerState();
            view |= state.viewDistance >= 0; simulation |= state.simulationDistance >= 0;
        }
        if (view || meldrite$weightedView) {
            boolean changed = view != meldrite$weightedView;
            meldrite$weightedView = view;
            if (changed) meldrite$nearby.meldrite$setRadius(view ? 32 : meldrite$worldViewDistance);
            var centers = new LongOpenHashSet(meldrite$viewCenters);
            centers.addAll(playersByChunkPos.keySet());
            for (long chunk : centers) {
                var players = playersByChunkPos.get(chunk);
                int level = players == null || players.isEmpty() ? Integer.MAX_VALUE : view ? meldrite$viewSourceLevel(chunk) : 0;
                meldrite$nearby.meldrite$source(chunk, level);
            }
            meldrite$viewCenters.clear();
            if (view) meldrite$viewCenters.addAll(playersByChunkPos.keySet());
        }
        if (simulation || meldrite$weightedSimulation) {
            var centers = new LongOpenHashSet(meldrite$simulationLevels.keySet());
            centers.addAll(playersByChunkPos.keySet());
            int defaultLevel = meldrite$simulationLevel(simulationDistance);
            for (long chunk : centers) {
                var position = new ChunkPos(chunk);
                // Enter/leave still perform their native default-ticket operation before this hook.
                simulationDistanceTracker.remove(ChunkTicketType.PLAYER, position, defaultLevel, position);
                if (meldrite$simulationLevels.containsKey(chunk)) simulationDistanceTracker.remove(ChunkTicketType.PLAYER, position, meldrite$simulationLevels.get(chunk), position);
                var players = playersByChunkPos.get(chunk);
                if (players == null || players.isEmpty()) { meldrite$simulationLevels.remove(chunk); continue; }
                int radius = 0;
                for (var player : players) radius = Math.max(radius, NativePlayerDistances.simulation(player, simulationDistance));
                int level = meldrite$simulationLevel(radius);
                simulationDistanceTracker.add(ChunkTicketType.PLAYER, position, level, position);
                if (simulation) meldrite$simulationLevels.put(chunk, level); else meldrite$simulationLevels.remove(chunk);
            }
            meldrite$weightedSimulation = simulation;
        }
    }
    @Unique private static int meldrite$simulationLevel(int distance) { return Math.max(0, ChunkLevels.getLevelFromType(ChunkLevelType.ENTITY_TICKING) - distance); }
    @Override public void meldrite$flushDistanceSources() { meldrite$nearby.meldrite$flushSources(); simulationDistanceTracker.updateLevels(); }
    @Override public int meldrite$loadingLevel(long chunk) { return meldrite$nearby.meldrite$sourceLevel(chunk); }
}
