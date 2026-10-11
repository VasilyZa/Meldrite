package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.HorseEntityPaperAccessMixin;
import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.HorseMarking;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryHorse;

public class CraftHorse extends CraftAbstractHorse implements org.bukkit.entity.Horse {
    public CraftHorse(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public HorseEntity getHandle() { return (HorseEntity) super.getHandle(); }
    @Override public Color getColor() { return Color.values()[getHandle().getVariant().getId()]; }
    @Override public void setColor(Color color) { java.util.Objects.requireNonNull(color, "color"); requireWritable(); ((HorseEntityPaperAccessMixin) getHandle()).meldrite$setVariant(HorseColor.byId(color.ordinal()), getHandle().getMarking()); }
    @Override public Style getStyle() { return Style.values()[getHandle().getMarking().getId()]; }
    @Override public void setStyle(Style style) { java.util.Objects.requireNonNull(style, "style"); requireWritable(); ((HorseEntityPaperAccessMixin) getHandle()).meldrite$setVariant(getHandle().getVariant(), HorseMarking.byIndex(style.ordinal())); }
    @Override public boolean isCarryingChest() { return false; }
    @Override public void setCarryingChest(boolean value) { throw new UnsupportedOperationException("Horse cannot carry a chest"); }
    @Override public org.bukkit.inventory.HorseInventory getInventory() { return new CraftInventoryHorse(horseAccess().meldrite$getInventory(), this); }
}
