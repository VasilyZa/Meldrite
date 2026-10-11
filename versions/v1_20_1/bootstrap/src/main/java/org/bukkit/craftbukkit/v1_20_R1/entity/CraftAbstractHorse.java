package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeHorseState;
import dev.meldrite.v1201.hook.AbstractHorseEntityPaperAccessMixin;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftSaddledInventory;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Horse;

public class CraftAbstractHorse extends CraftAnimals implements org.bukkit.entity.AbstractHorse {
    public CraftAbstractHorse(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AbstractHorseEntity getHandle() { return (AbstractHorseEntity) super.getHandle(); }
    protected final AbstractHorseEntityPaperAccessMixin horseAccess() { return (AbstractHorseEntityPaperAccessMixin) getHandle(); }
    @Override public Horse.Variant getVariant() { return Horse.Variant.HORSE; }
    @Override public void setVariant(Horse.Variant variant) { throw new UnsupportedOperationException("Horse variant is determined by the entity type"); }
    @Override public int getDomestication() { return getHandle().getTemper(); }
    @Override public void setDomestication(int value) { if (value < 0 || value > getMaxDomestication()) throw new IllegalArgumentException("Domestication must be between 0 and the maximum"); requireWritable(); getHandle().setTemper(value); }
    @Override public int getMaxDomestication() { return getHandle().getMaxTemper(); }
    @Override public void setMaxDomestication(int value) { if (value <= 0) throw new IllegalArgumentException("Max domestication must be positive"); requireWritable(); ((NativeHorseState) getHandle()).meldrite$maxDomestication(value); }
    @Override public double getJumpStrength() { return getHandle().getJumpStrength(); }
    @Override public void setJumpStrength(double value) { if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException("Jump strength must be finite and nonnegative"); requireWritable(); getHandle().getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH).setBaseValue(value); }
    @Override public boolean isTamed() { return getHandle().isTame(); }
    @Override public void setTamed(boolean value) { requireWritable(); getHandle().setTame(value); }
    @Override public java.util.UUID getOwnerUniqueId() { return getHandle().getOwnerUuid(); }
    public java.util.UUID getOwnerUUID() { return getOwnerUniqueId(); }
    public void setOwnerUUID(java.util.UUID uuid) { requireWritable(); getHandle().setOwnerUuid(uuid); }
    @Override public AnimalTamer getOwner() { var uuid = getOwnerUniqueId(); return uuid == null ? null : getServer().getOfflinePlayer(uuid); }
    @Override public void setOwner(AnimalTamer owner) { requireWritable(); setTamed(owner != null); setOwnerUUID(owner == null ? null : owner.getUniqueId()); if (owner != null) ((dev.meldrite.v1201.compat.MobNativeAccess) getHandle()).meldrite$setTarget(null, null, false); }
    @Override public boolean isEatingHaystack() { return isEatingGrass(); }
    @Override public void setEatingHaystack(boolean value) { setEatingGrass(value); }
    @Override public boolean isEatingGrass() { return getHandle().isEatingGrass(); }
    @Override public void setEatingGrass(boolean value) { requireWritable(); getHandle().setEatingGrass(value); }
    @Override public boolean isRearing() { return getHandle().isAngry(); }
    @Override public void setRearing(boolean value) { requireWritable(); ((NativeHorseState) getHandle()).meldrite$forceRearing(value); }
    @Override public boolean isEating() { return horseAccess().meldrite$getHorseFlag(64); }
    @Override public void setEating(boolean value) { requireWritable(); horseAccess().meldrite$setHorseFlag(64, value); horseAccess().meldrite$setEatingTicks(value ? 1 : 0); }
    @Override public org.bukkit.inventory.AbstractHorseInventory getInventory() { return new CraftSaddledInventory(horseAccess().meldrite$getInventory(), this); }
}
