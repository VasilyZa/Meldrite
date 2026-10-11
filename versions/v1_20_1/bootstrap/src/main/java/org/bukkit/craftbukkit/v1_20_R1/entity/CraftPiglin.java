package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativePiglinItems;
import dev.meldrite.v1201.hook.PiglinEntityPaperAccessMixin;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PiglinEntity;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
public class CraftPiglin extends CraftPiglinAbstract implements org.bukkit.entity.Piglin, com.destroystokyo.paper.entity.CraftRangedEntity<PiglinEntity> {
    public CraftPiglin(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public PiglinEntity getHandle() { return (PiglinEntity) super.getHandle(); }
    private NativePiglinItems items() { return (NativePiglinItems) getHandle(); }
    private PiglinEntityPaperAccessMixin access() { return (PiglinEntityPaperAccessMixin) getHandle(); }
    @Override public boolean isAbleToHunt() { return access().meldrite$cannotHunt(); }
    @Override public void setIsAbleToHunt(boolean value) { requireWritable(); access().meldrite$cannotHunt(value); }
    private net.minecraft.item.Item item(Material material) { var item = CraftMagicNumbers.getItem(java.util.Objects.requireNonNull(material, "material")); if (item == null) throw new IllegalArgumentException("Material must be an item"); return item; }
    @Override public boolean addBarterMaterial(Material material) { var item = item(material); requireWritable(); return items().meldrite$barterItems().add(item); }
    @Override public boolean removeBarterMaterial(Material material) { var item = item(material); requireWritable(); return items().meldrite$barterItems().remove(item); }
    @Override public boolean addMaterialOfInterest(Material material) { var item = item(material); requireWritable(); return items().meldrite$interestItems().add(item); }
    @Override public boolean removeMaterialOfInterest(Material material) { var item = item(material); requireWritable(); return items().meldrite$interestItems().remove(item); }
    @Override public java.util.Set<Material> getInterestList() { return items().meldrite$interestItems().stream().map(CraftMagicNumbers::getMaterial).collect(java.util.stream.Collectors.toUnmodifiableSet()); }
    @Override public java.util.Set<Material> getBarterList() { return items().meldrite$barterItems().stream().map(CraftMagicNumbers::getMaterial).collect(java.util.stream.Collectors.toUnmodifiableSet()); }
    @Override public org.bukkit.inventory.Inventory getInventory() { return new CraftInventory(getHandle().getInventory(), this); }
    @Override public void setChargingCrossbow(boolean value) { requireWritable(); getHandle().setCharging(value); }
    @Override public boolean isChargingCrossbow() { return access().meldrite$isCharging(); }
    @Override public void setDancing(boolean value) { requireWritable(); var brain = getHandle().getBrain(); if (value) { brain.remember(MemoryModuleType.DANCING, true); brain.remember(MemoryModuleType.CELEBRATE_LOCATION, getHandle().getLandingPos()); } else { brain.forget(MemoryModuleType.DANCING); brain.forget(MemoryModuleType.CELEBRATE_LOCATION); } }
    @Override public void setDancing(long duration) { requireWritable(); getHandle().getBrain().remember(MemoryModuleType.DANCING, true, duration); getHandle().getBrain().remember(MemoryModuleType.CELEBRATE_LOCATION, getHandle().getLandingPos(), duration); }
    @Override public boolean isDancing() { return getHandle().isDancing(); }
}
