package dev.meldrite.v1201.hook;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityAccess {
    @Accessor("ITEM") static TrackedData<ItemStack> meldrite$itemKey() { throw new AssertionError("Firework accessor mixin was not applied"); }
    @Accessor("SHOT_AT_ANGLE") static TrackedData<Boolean> meldrite$shotAtAngleKey() { throw new AssertionError("Firework accessor mixin was not applied"); }
    @Accessor("life") int meldrite$getLife();
    @Accessor("life") void meldrite$setLife(int value);
    @Accessor("lifeTime") int meldrite$getLifeTime();
    @Accessor("lifeTime") void meldrite$setLifeTime(int value);
    @Accessor("shooter") LivingEntity meldrite$getShooter();
    @Accessor("shooter") void meldrite$setShooter(LivingEntity value);
}
