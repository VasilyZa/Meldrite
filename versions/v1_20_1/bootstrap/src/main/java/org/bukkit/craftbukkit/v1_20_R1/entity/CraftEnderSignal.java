package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.EyeOfEnderEntityPaperAccessMixin;
import net.minecraft.entity.EyeOfEnderEntity;

public final class CraftEnderSignal extends CraftEntity implements org.bukkit.entity.EnderSignal {
    public CraftEnderSignal(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public EyeOfEnderEntity getHandle() { return (EyeOfEnderEntity) super.getHandle(); }
    private EyeOfEnderEntityPaperAccessMixin state() { return (EyeOfEnderEntityPaperAccessMixin) (Object) getHandle(); }
    @Override public org.bukkit.Location getTargetLocation() { return new org.bukkit.Location(getWorld(), state().meldrite$getTargetX(), state().meldrite$getTargetY(), state().meldrite$getTargetZ()); }
    @Override public void setTargetLocation(org.bukkit.Location location) { setTargetLocation(location, getDropItem()); }
    @Override public void setTargetLocation(org.bukkit.Location location, boolean dropItem) { java.util.Objects.requireNonNull(location, "location").checkFinite(); if (location.getWorld() != getWorld()) throw new IllegalArgumentException("TARGET_DIFFERENT_WORLD"); delegate.requireWritable(); state().meldrite$setTargetX(location.getX()); state().meldrite$setTargetY(location.getY()); state().meldrite$setTargetZ(location.getZ()); state().meldrite$setDropsItem(dropItem); }
    @Override public boolean getDropItem() { return state().meldrite$getDropsItem(); }
    @Override public void setDropItem(boolean dropItem) { delegate.requireWritable(); state().meldrite$setDropsItem(dropItem); }
    @Override public org.bukkit.inventory.ItemStack getItem() { return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(getHandle().getStack()); }
    @Override public void setItem(org.bukkit.inventory.ItemStack item) { java.util.Objects.requireNonNull(item, "item"); if (item.getType().isAir()) throw new IllegalArgumentException("Ender signal item cannot be empty"); delegate.requireWritable(); getHandle().setItem(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(item)); }
    @Override public int getDespawnTimer() { return state().meldrite$getLifespan(); }
    @Override public void setDespawnTimer(int ticks) { if (ticks < 0) throw new IllegalArgumentException("despawn timer must be nonnegative"); delegate.requireWritable(); state().meldrite$setLifespan(ticks); }
}
