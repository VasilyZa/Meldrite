package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.ShulkerBulletNativeAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerBulletEntity.class)
public interface ShulkerBulletPaperAccessMixin extends ShulkerBulletNativeAccess {
    @Override @Accessor("target") Entity meldrite$getTarget();
    @Override @Accessor("target") void meldrite$setTarget(Entity value);
    @Override @Accessor("direction") Direction meldrite$getDirection();
    @Override @Accessor("direction") void meldrite$setDirection(Direction value);
    @Override @Accessor("stepCount") int meldrite$getFlightSteps();
    @Override @Accessor("stepCount") void meldrite$setFlightSteps(int value);
    @Override @Accessor("targetX") double meldrite$getTargetX();
    @Override @Accessor("targetX") void meldrite$setTargetX(double value);
    @Override @Accessor("targetY") double meldrite$getTargetY();
    @Override @Accessor("targetY") void meldrite$setTargetY(double value);
    @Override @Accessor("targetZ") double meldrite$getTargetZ();
    @Override @Accessor("targetZ") void meldrite$setTargetZ(double value);
}
