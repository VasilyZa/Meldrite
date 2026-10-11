package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.ExperienceOrbNativeAccess;
import net.minecraft.entity.ExperienceOrbEntity;
public final class CraftExperienceOrb extends CraftEntity implements org.bukkit.entity.ExperienceOrb {
    public CraftExperienceOrb(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ExperienceOrbEntity getHandle() { return (ExperienceOrbEntity) super.getHandle(); }
    private void requireWritable() { delegate.requireWritable(); }
    private ExperienceOrbNativeAccess state() { return (ExperienceOrbNativeAccess) (Object) getHandle(); }
    @Override public int getExperience() { return state().meldrite$getAmount(); }
    @Override public void setExperience(int value) { if (value < 0) throw new IllegalArgumentException("experience must be nonnegative"); requireWritable(); state().meldrite$setAmount(value); }
    @Override public java.util.UUID getTriggerEntityId() { return state().meldrite$getTriggerEntityId(); }
    @Override public java.util.UUID getSourceEntityId() { return state().meldrite$getSourceEntityId(); }
    @Override public org.bukkit.entity.ExperienceOrb.SpawnReason getSpawnReason() { return state().meldrite$getSpawnReason(); }
}
