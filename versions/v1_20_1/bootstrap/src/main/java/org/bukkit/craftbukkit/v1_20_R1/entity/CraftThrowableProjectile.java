package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;

public class CraftThrowableProjectile extends CraftProjectile implements org.bukkit.entity.ThrowableProjectile {
    public CraftThrowableProjectile(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ThrownItemEntity getHandle() { return (ThrownItemEntity) super.getHandle(); }
    @Override public org.bukkit.inventory.ItemStack getItem() { return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(getHandle().getStack()); }
    @Override public void setItem(org.bukkit.inventory.ItemStack item) {
        java.util.Objects.requireNonNull(item, "item");
        if (item.getType().isAir()) throw new IllegalArgumentException("Thrown item cannot be empty");
        delegate.requireWritable();
        getHandle().setItem(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(item));
    }
}
