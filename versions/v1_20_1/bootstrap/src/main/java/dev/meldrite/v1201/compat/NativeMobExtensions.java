package dev.meldrite.v1201.compat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public final class NativeMobExtensions {
    private NativeMobExtensions() {}
    public static EntityTargetLivingEntityEvent targetEvent(MobEntity mob, LivingEntity target, EntityTargetEvent.TargetReason reason) {
        NativeEntityExtensions.requireThread(mob);
        if (reason == null) reason = EntityTargetEvent.TargetReason.UNKNOWN;
        if (reason == EntityTargetEvent.TargetReason.UNKNOWN && mob.getTarget() != null && target == null) {
            reason = mob.getTarget().isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
        }
        var bukkit = NativeEntityExtensions.bukkit(mob);
        var projectedTarget = target == null ? null : (CraftLivingEntity) NativeEntityExtensions.bukkit(target);
        var event = new EntityTargetLivingEntityEvent(bukkit, projectedTarget, reason);
        bukkit.getServer().getPluginManager().callEvent(event);
        if (event.getTarget() != null && (!(event.getTarget() instanceof CraftLivingEntity craft) || craft.getHandle().getWorld() != mob.getWorld())) {
            throw new IllegalArgumentException("FOREIGN_TARGET_ENTITY");
        }
        return event;
    }
}
