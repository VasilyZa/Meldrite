package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.TameableEntity;

public class CraftTameableAnimal extends CraftAnimals implements org.bukkit.entity.Tameable, org.bukkit.entity.Sittable {
    public CraftTameableAnimal(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public TameableEntity getHandle() { return (TameableEntity) super.getHandle(); }
    @Override public boolean isTamed() { return getHandle().isTamed(); }
    @Override public void setTamed(boolean tame) { requireWritable(); getHandle().setTamed(tame); if (!tame) getHandle().setOwnerUuid(null); }
    @Override public java.util.UUID getOwnerUniqueId() { return getHandle().getOwnerUuid(); }
    public java.util.UUID getOwnerUUID() { return getOwnerUniqueId(); }
    public void setOwnerUUID(java.util.UUID uuid) { requireWritable(); getHandle().setOwnerUuid(uuid); }
    @Override public org.bukkit.entity.AnimalTamer getOwner() {
        var uuid = getOwnerUniqueId();
        if (uuid == null) return null;
        var player = getServer().getPlayer(uuid);
        return player != null ? player : getServer().getOfflinePlayer(uuid);
    }
    @Override public void setOwner(org.bukkit.entity.AnimalTamer owner) {
        requireWritable();
        if (owner == null) { setTamed(false); return; }
        getHandle().setTamed(true); getHandle().setOwnerUuid(owner.getUniqueId());
        ((dev.meldrite.v1201.compat.MobNativeAccess) getHandle()).meldrite$setTarget(null, null, false);
    }
    @Override public boolean isSitting() { return getHandle().isInSittingPose(); }
    @Override public void setSitting(boolean sitting) { requireWritable(); getHandle().setInSittingPose(sitting); getHandle().setSitting(sitting); }
}
