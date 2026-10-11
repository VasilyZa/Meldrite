package dev.meldrite.v1201.hook;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ProjectileEntity.class)
public interface ProjectileEntityAccess {
    @Accessor("owner") Entity meldrite$getOwner();
    @Accessor("owner") void meldrite$setOwner(Entity owner);
    @Accessor("ownerUuid") UUID meldrite$getOwnerUuid();
    @Accessor("ownerUuid") void meldrite$setOwnerUuid(UUID ownerUuid);
    @Accessor("leftOwner") boolean meldrite$hasLeftOwner();
    @Accessor("leftOwner") void meldrite$setLeftOwner(boolean value);
    @Accessor("shot") boolean meldrite$hasBeenShot();
    @Accessor("shot") void meldrite$setBeenShot(boolean value);
    @Invoker("canHit") boolean meldrite$canHit(Entity entity);
    @Invoker("onEntityHit") void meldrite$onEntityHit(EntityHitResult result);
    @Invoker("onCollision") void meldrite$onCollision(HitResult result);
}
