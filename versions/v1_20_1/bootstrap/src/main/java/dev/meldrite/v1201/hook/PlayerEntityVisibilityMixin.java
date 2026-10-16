package dev.meldrite.v1201.hook;
import dev.meldrite.v1201.compat.NativeEntityTrackerAccess;
import dev.meldrite.v1201.compat.NativePlayerVisibility;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets = "net.minecraft.server.world.ThreadedAnvilChunkStorage$EntityTracker")
public abstract class PlayerEntityVisibilityMixin implements NativeEntityTrackerAccess {
    @Shadow @Final private Entity entity;
    @Override @Invoker("stopTracking") public abstract void meldrite$stopTracking(ServerPlayerEntity viewer);
    @Override @Invoker("updateTrackedStatus") public abstract void meldrite$updateTrackedStatus(ServerPlayerEntity viewer);
    @org.spongepowered.asm.mixin.injection.Redirect(method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;watchDistance:I", opcode = org.objectweb.asm.Opcodes.GETFIELD))
    private int meldrite$watchDistance(net.minecraft.server.world.ThreadedAnvilChunkStorage storage, ServerPlayerEntity viewer) {
        int world = dev.meldrite.v1201.compat.NativePlayerDistances.worldView(storage);
        return dev.meldrite.v1201.compat.NativePlayerDistances.view(viewer, world);
    }
    @Inject(method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("HEAD"), cancellable = true)
    private void meldrite$visibility(ServerPlayerEntity viewer, CallbackInfo ci) {
        if (!NativePlayerVisibility.canSee(viewer, entity)) { meldrite$stopTracking(viewer); ci.cancel(); }
    }
}
