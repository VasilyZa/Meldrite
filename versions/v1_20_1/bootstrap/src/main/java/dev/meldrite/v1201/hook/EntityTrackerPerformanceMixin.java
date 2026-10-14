package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.perf.TrackingBatch;
import dev.meldrite.v1201.perf.TrackingSnapshotSource;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.world.ThreadedAnvilChunkStorage$EntityTracker", priority = 900)
public abstract class EntityTrackerPerformanceMixin implements TrackingSnapshotSource {
    @Shadow @Final private Entity entity;
    @Shadow @Final private Set<EntityTrackingListener> listeners;
    @Invoker("getMaxTrackDistance") protected abstract int meldrite$maxTrackDistance();
    @Override public Entity meldrite$trackedEntity() { return entity; }
    @Override public int meldrite$trackingRange() { return meldrite$maxTrackDistance(); }
    @Override public boolean meldrite$hasTrackingListener(ServerPlayerEntity player) { return listeners.contains(player.networkHandler); }
    @Inject(method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("HEAD"), cancellable = true)
    private void meldrite$outsideTracking(ServerPlayerEntity player, CallbackInfo ci) {
        if (TrackingBatch.outside(this, player)) ci.cancel();
    }
}
