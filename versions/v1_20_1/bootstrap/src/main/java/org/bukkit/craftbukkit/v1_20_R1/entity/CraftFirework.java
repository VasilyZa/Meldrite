package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.FireworkRocketEntityAccess;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;

public class CraftFirework extends CraftProjectile implements org.bukkit.entity.Firework {
    public CraftFirework(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.projectile.FireworkRocketEntity getHandle() { return (net.minecraft.entity.projectile.FireworkRocketEntity) super.getHandle(); }
    private FireworkRocketEntityAccess state() { return (FireworkRocketEntityAccess) (Object) getHandle(); }
    private ItemStack nativeItem() { return getHandle().getStack(); }
    private TrackedData<ItemStack> itemKey() { return FireworkRocketEntityAccess.meldrite$itemKey(); }
    private TrackedData<Boolean> angleKey() { return FireworkRocketEntityAccess.meldrite$shotAtAngleKey(); }
    @Override public org.bukkit.inventory.meta.FireworkMeta getFireworkMeta() { return (org.bukkit.inventory.meta.FireworkMeta) org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.getItemMeta(nativeItem(), org.bukkit.Material.FIREWORK_ROCKET); }
    @Override public void setFireworkMeta(org.bukkit.inventory.meta.FireworkMeta meta) { java.util.Objects.requireNonNull(meta, "meta"); delegate.requireWritable(); ItemStack item = nativeItem().copy(); org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.applyMetaToItem(item, meta); getHandle().getDataTracker().set(itemKey(), item); state().meldrite$setLifeTime(10 * (1 + meta.getPower()) + java.util.concurrent.ThreadLocalRandom.current().nextInt(6) + java.util.concurrent.ThreadLocalRandom.current().nextInt(7)); }
    @Override public boolean setAttachedTo(org.bukkit.entity.LivingEntity entity) { if (isDetonated()) return false; delegate.requireWritable(); if (entity == null) { state().meldrite$setShooter(null); return true; } if (!(entity instanceof CraftLivingEntity living)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE"); state().meldrite$setShooter(living.getHandle()); return true; }
    @Override public org.bukkit.entity.LivingEntity getAttachedTo() { LivingEntity entity = state().meldrite$getShooter(); return entity == null ? null : (org.bukkit.entity.LivingEntity) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity); }
    @Override public boolean setLife(int life) { if (life < 0) throw new IllegalArgumentException("life must be nonnegative"); if (isDetonated()) return false; delegate.requireWritable(); state().meldrite$setLife(life); return true; }
    @Override public int getLife() { return state().meldrite$getLife(); }
    @Override public boolean setMaxLife(int life) { if (life <= 0) throw new IllegalArgumentException("life must be positive"); if (isDetonated()) return false; delegate.requireWritable(); state().meldrite$setLifeTime(life); return true; }
    @Override public int getMaxLife() { return state().meldrite$getLifeTime(); }
    @Override public void detonate() { if (!isDetonated()) { delegate.requireWritable(); state().meldrite$setLife(state().meldrite$getLifeTime() + 1); } }
    @Override public boolean isDetonated() { return state().meldrite$getLife() > state().meldrite$getLifeTime() || getHandle().isRemoved(); }
    @Override public boolean isShotAtAngle() { return getHandle().getDataTracker().get(angleKey()); }
    @Override public void setShotAtAngle(boolean value) { delegate.requireWritable(); getHandle().getDataTracker().set(angleKey(), value); }
    @Override public java.util.UUID getSpawningEntity() { return getHandle().getOwner() == null ? null : getHandle().getOwner().getUuid(); }
    @Override public org.bukkit.inventory.ItemStack getItem() { return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(nativeItem()); }
    @Override public void setItem(org.bukkit.inventory.ItemStack item) { java.util.Objects.requireNonNull(item, "item"); delegate.requireWritable(); ItemStack nms = org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(item); if (nms.isEmpty()) nms = new ItemStack(net.minecraft.item.Items.FIREWORK_ROCKET); getHandle().getDataTracker().set(itemKey(), nms); }
    @Override public int getTicksFlown() { return state().meldrite$getLife(); }
    @Override public void setTicksFlown(int ticks) { if (ticks < 0) throw new IllegalArgumentException("ticks must be nonnegative"); delegate.requireWritable(); state().meldrite$setLife(ticks); }
    @Override public int getTicksToDetonate() { return state().meldrite$getLifeTime() - state().meldrite$getLife(); }
    @Override public void setTicksToDetonate(int ticks) { if (ticks < 0) throw new IllegalArgumentException("ticks must be nonnegative"); delegate.requireWritable(); state().meldrite$setLifeTime(state().meldrite$getLife() + ticks); }
}
