package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CraftSizedFireball extends CraftFireball implements org.bukkit.entity.SizedFireball {
    public CraftSizedFireball(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AbstractFireballEntity getHandle() { return (AbstractFireballEntity) super.getHandle(); }
    @Override public ItemStack getDisplayItem() { var stack = getHandle().getStack(); return stack.isEmpty() ? new ItemStack(Material.FIRE_CHARGE) : org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(stack); }
    @Override public void setDisplayItem(ItemStack item) { java.util.Objects.requireNonNull(item, "item"); delegate.requireWritable(); getHandle().setItem(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(item)); }
}
