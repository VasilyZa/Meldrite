package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import dev.meldrite.v1201.compat.NativeEntityTeleport;
import dev.meldrite.v1201.compat.NativeEntitySchedulerLifecycle;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityTeleportFacadeMixin {
    @Inject(method = "setWorld", at = @At("TAIL"))
    private void meldrite$rebindSameEntity(net.minecraft.world.World world, CallbackInfo callback) {
        NativeEntityTeleport.entityWorldChanged((Entity) (Object) this);
    }
    @WrapOperation(method = {"teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z", "moveToWorld"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onDimensionChanged(Lnet/minecraft/entity/Entity;)V"), require = 1)
    private void meldrite$rebindBeforeInsertion(ServerWorld world, Entity replacement, Operation<Void> original) {
        NativeEntityTeleport.beforeDimensionInsert((Entity) (Object) this, replacement);
        original.call(world, replacement);
    }

    @WrapMethod(method = "moveToWorld")
    private Entity meldrite$finishNativeTransfer(ServerWorld world, Operation<Entity> original) {
        try { return original.call(world); }
        finally { NativeEntitySchedulerLifecycle.dimensionTransferCompleted((Entity) (Object) this); }
    }
}
