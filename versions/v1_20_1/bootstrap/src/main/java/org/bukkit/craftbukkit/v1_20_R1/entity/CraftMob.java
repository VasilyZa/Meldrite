package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.MobNativeAccess;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.MobEntityPaperAccessMixin;
import net.minecraft.entity.mob.MobEntity;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.loot.LootTable;

public class CraftMob extends CraftLivingEntity implements Mob {
    private final com.destroystokyo.paper.entity.Pathfinder pathfinder = new dev.meldrite.v1201.compat.NativePathfinder(this);
    public CraftMob(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public MobEntity getHandle() { return (MobEntity) super.getHandle(); }
    private MobEntityPaperAccessMixin mobState() { return (MobEntityPaperAccessMixin) getHandle(); }
    private MobNativeAccess extensions() { return (MobNativeAccess) getHandle(); }
    @Override public com.destroystokyo.paper.entity.Pathfinder getPathfinder() { return pathfinder; }
    @Override public void setTarget(LivingEntity target) { requireWritable(); if (target != null && !(target instanceof CraftLivingEntity)) throw new IllegalArgumentException("FOREIGN_TARGET_ENTITY"); extensions().meldrite$setTarget(target == null ? null : ((CraftLivingEntity) target).getHandle(), null, false); }
    @Override public CraftLivingEntity getTarget() { var target = getHandle().getTarget(); return target == null ? null : (CraftLivingEntity) NativeEntityExtensions.bukkit(target); }
    @Override public void setAware(boolean aware) { requireWritable(); extensions().meldrite$setAware(aware); }
    @Override public boolean isAware() { return extensions().meldrite$isAware(); }
    @Override public Sound getAmbientSound() { var sound = mobState().meldrite$ambientSound(); if (sound == null) return null; var id = sound.getId(); return org.bukkit.Registry.SOUNDS.get(new NamespacedKey(id.getNamespace(), id.getPath())); }
    @Override public boolean isInDaylight() { return mobState().meldrite$isInDaylight(); }
    @Override public void setLootTable(LootTable table) { requireWritable(); mobState().meldrite$setLootTable(table == null ? null : new net.minecraft.util.Identifier(table.getKey().getNamespace(), table.getKey().getKey())); }
    @Override public LootTable getLootTable() { var id = getHandle().getLootTable(); var manager = ((net.minecraft.server.world.ServerWorld) getHandle().getWorld()).getServer().getLootManager(); return dev.meldrite.v1201.compat.NativeLootTables.get(manager, id); }
    @Override public void setSeed(long seed) { requireWritable(); mobState().meldrite$setLootTableSeed(seed); }
    @Override public long getSeed() { return getHandle().getLootTableSeed(); }
    @Override public void lookAt(Location location) { java.util.Objects.requireNonNull(location).checkFinite(); if (location.getWorld() != getWorld()) throw new IllegalArgumentException("LOOK_LOCATION_IN_DIFFERENT_WORLD"); lookAt(location.getX(), location.getY(), location.getZ()); }
    @Override public void lookAt(Location location, float speed, float pitch) { java.util.Objects.requireNonNull(location).checkFinite(); if (location.getWorld() != getWorld()) throw new IllegalArgumentException("LOOK_LOCATION_IN_DIFFERENT_WORLD"); lookAt(location.getX(), location.getY(), location.getZ(), speed, pitch); }
    private net.minecraft.entity.Entity nativeTarget(org.bukkit.entity.Entity entity) { if (!(entity instanceof CraftEntity craft)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE"); if (craft.getWorld() != getWorld()) throw new IllegalArgumentException("LOOK_TARGET_IN_DIFFERENT_WORLD"); return craft.getHandle(); }
    @Override public void lookAt(org.bukkit.entity.Entity entity) { requireWritable(); getHandle().getLookControl().lookAt(nativeTarget(entity)); }
    @Override public void lookAt(org.bukkit.entity.Entity entity, float speed, float pitch) { limits(speed, pitch); requireWritable(); getHandle().getLookControl().lookAt(nativeTarget(entity), speed, pitch); }
    private static void limits(float speed, float pitch) { if (!Float.isFinite(speed) || !Float.isFinite(pitch)) throw new IllegalArgumentException("Nonfinite look limits"); }
    private static void coordinates(double x, double y, double z) { if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) throw new IllegalArgumentException("Nonfinite look coordinates"); }
    @Override public void lookAt(double x, double y, double z) { coordinates(x, y, z); requireWritable(); getHandle().getLookControl().lookAt(x, y, z); }
    @Override public void lookAt(double x, double y, double z, float speed, float pitch) { coordinates(x, y, z); limits(speed, pitch); requireWritable(); getHandle().getLookControl().lookAt(x, y, z, speed, pitch); }
    @Override public int getHeadRotationSpeed() { return getHandle().getMaxLookYawChange(); }
    @Override public int getMaxHeadPitch() { return getHandle().getMaxLookPitchChange(); }
    @Override public boolean isLeftHanded() { return getHandle().isLeftHanded(); }
    @Override public void setLeftHanded(boolean leftHanded) { requireWritable(); getHandle().setLeftHanded(leftHanded); }
    @Override public int getPossibleExperienceReward() { return getHandle().getXpToDrop(); }
    @Override public boolean isLeashed() { return getHandle().isLeashed(); }
    @Override public org.bukkit.entity.Entity getLeashHolder() { if (!isLeashed()) throw new IllegalStateException("ENTITY_NOT_LEASHED"); return NativeEntityExtensions.bukkit(getHandle().getHoldingEntity()); }
    @Override public boolean setLeashHolder(org.bukkit.entity.Entity holder) { requireWritable(); if (getHandle() instanceof net.minecraft.entity.boss.WitherEntity) return false; if (holder == null) { if (!isLeashed()) return false; getHandle().detachLeash(true, false); return true; } var target = nativeTarget(holder); if (!target.isAlive()) return false; getHandle().detachLeash(true, false); getHandle().attachLeash(target, true); return true; }
}
