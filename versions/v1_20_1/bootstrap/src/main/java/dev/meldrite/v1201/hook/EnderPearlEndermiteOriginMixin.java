package dev.meldrite.v1201.hook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeEndermiteState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEndermiteOriginMixin {
    @WrapOperation(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean meldrite$playerOrigin(World world, Entity entity, Operation<Boolean> original) { if (entity instanceof NativeEndermiteState mite) mite.meldrite$playerSpawned(true); return original.call(world, entity); }
}
