package com.destroystokyo.paper.entity;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.MobEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;

public interface CraftRangedEntity<T extends MobEntity & RangedAttackMob> extends RangedEntity {
    T getHandle();
    @Override default void rangedAttack(org.bukkit.entity.LivingEntity target, float charge) {
        java.util.Objects.requireNonNull(target, "target");
        if (!(target instanceof CraftLivingEntity living)) throw new IllegalArgumentException("Target must be native-backed");
        if (!Float.isFinite(charge)) throw new IllegalArgumentException("Charge must be finite");
        NativeEntityExtensions.requireThread(getHandle());
        getHandle().attack(living.getHandle(), charge);
    }
    @Override default void setChargingAttack(boolean value) { NativeEntityExtensions.requireThread(getHandle()); getHandle().setAttacking(value); }
    @Override default boolean isChargingAttack() { return getHandle().isAttacking(); }
}
