package dev.meldrite.v1201.hook;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.world.RaycastContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Leaf permits world rays without an entity and uses an empty collision context. */
@Mixin(RaycastContext.class)
public abstract class RaycastContextMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/ShapeContext;of(Lnet/minecraft/entity/Entity;)Lnet/minecraft/block/ShapeContext;"))
    private ShapeContext meldrite$nullableEntity(Entity entity, Operation<ShapeContext> original) {
        return entity == null ? ShapeContext.absent() : original.call(entity);
    }
}
