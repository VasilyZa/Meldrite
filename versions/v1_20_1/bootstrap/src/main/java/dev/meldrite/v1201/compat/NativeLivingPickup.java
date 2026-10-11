package dev.meldrite.v1201.compat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

/** LivingEntity item pickup follows vanilla mobs and Bukkit's non-mob flag. */
public final class NativeLivingPickup {
    private NativeLivingPickup() { }
    public static boolean get(LivingEntity entity) {
        if (entity instanceof MobEntity mob) return mob.canPickUpLoot();
        Boolean override = state(entity).meldrite$getPickupOverride();
        return override == null ? entity instanceof PlayerEntity : override;
    }
    public static void set(LivingEntity entity, boolean pickup) {
        NativeEntityExtensions.requireThread(entity);
        if (entity instanceof MobEntity mob) mob.setCanPickUpLoot(pickup);
        else state(entity).meldrite$setPickupOverride(pickup);
    }
    public static void pickup(PlayerEntity player, Runnable nativePickup) {
        // Do not change vanilla or mod pickup behavior until a plugin explicitly disables it.
        if (!Boolean.FALSE.equals(state(player).meldrite$getPickupOverride())) nativePickup.run();
    }
    private static PaperLivingPickupAccess state(LivingEntity entity) {
        if (!(entity instanceof PaperLivingPickupAccess access)) throw new IllegalStateException("LIVING_PICKUP_MIXIN_MISSING");
        return access;
    }
}
