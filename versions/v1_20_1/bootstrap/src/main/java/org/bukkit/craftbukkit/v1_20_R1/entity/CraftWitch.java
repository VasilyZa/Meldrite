package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.WitchEntityPaperAccessMixin;
import net.minecraft.entity.mob.WitchEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;

public class CraftWitch extends CraftRaider implements org.bukkit.entity.Witch, com.destroystokyo.paper.entity.CraftRangedEntity<WitchEntity> {
    public CraftWitch(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public WitchEntity getHandle() { return (WitchEntity) super.getHandle(); }
    @Override public boolean isDrinkingPotion() { return getHandle().isDrinking(); }
    @Override public int getPotionUseTimeLeft() { return ((WitchEntityPaperAccessMixin) getHandle()).meldrite$getDrinkTimeLeft(); }
    @Override public void setPotionUseTimeLeft(int ticks) { requireWritable(); ((WitchEntityPaperAccessMixin) getHandle()).meldrite$setDrinkTimeLeft(ticks); }
    @Override public org.bukkit.inventory.ItemStack getDrinkingPotion() { return CraftItemStack.asCraftMirror(getHandle().getMainHandStack()); }
    @Override public void setDrinkingPotion(org.bukkit.inventory.ItemStack potion) {
        if (potion != null && !potion.getType().isAir() && potion.getType() != org.bukkit.Material.POTION) throw new IllegalArgumentException("Must be potion, air or null");
        requireWritable(); dev.meldrite.v1201.compat.NativeWitchBehavior.drink(getHandle(), potion);
    }
}
