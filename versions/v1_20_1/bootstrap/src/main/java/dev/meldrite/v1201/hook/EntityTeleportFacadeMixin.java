package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeEntityTeleport;
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
    @WrapOperation(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onDimensionChanged(Lnet/minecraft/entity/Entity;)V"))
    private void meldrite$rebindBeforeInsertion(ServerWorld world, Entity replacement, Operation<Void> original) {
        NativeEntityTeleport.beforeDimensionInsert(replacement);
        original.call(world, replacement);
    }
}
