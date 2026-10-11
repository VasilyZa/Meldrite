package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.AnimalEntity;

public class CraftAnimals extends CraftAgeable implements org.bukkit.entity.Animals {
    public CraftAnimals(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AnimalEntity getHandle() { return (AnimalEntity) super.getHandle(); }
    @Override public java.util.UUID getBreedCause() { return ((dev.meldrite.v1201.hook.AnimalEntityPaperAccessMixin) getHandle()).meldrite$getLoveCause(); }
    @Override public void setBreedCause(java.util.UUID uuid) { requireWritable(); ((dev.meldrite.v1201.hook.AnimalEntityPaperAccessMixin) getHandle()).meldrite$setLoveCause(uuid); }
    @Override public boolean isLoveMode() { return getHandle().isInLove(); }
    @Override public int getLoveModeTicks() { return getHandle().getLoveTicks(); }
    @Override public void setLoveModeTicks(int ticks) { if (ticks < 0) throw new IllegalArgumentException("Love mode ticks must be nonnegative"); requireWritable(); getHandle().setLoveTicks(ticks); }
    @Override public boolean isBreedItem(org.bukkit.inventory.ItemStack item) { return item != null && !item.getType().isAir() && getHandle().isBreedingItem(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(item)); }
    @Override public boolean isBreedItem(org.bukkit.Material material) { return material != null && isBreedItem(new org.bukkit.inventory.ItemStack(material)); }
}
