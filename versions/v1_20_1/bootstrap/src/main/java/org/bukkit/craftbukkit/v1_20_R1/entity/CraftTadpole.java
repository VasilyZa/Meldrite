package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeAgeableState;
import dev.meldrite.v1201.hook.TadpoleEntityPaperAccessMixin;
import net.minecraft.entity.passive.TadpoleEntity;
public class CraftTadpole extends CraftFish implements org.bukkit.entity.Tadpole {
    public CraftTadpole(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public TadpoleEntity getHandle() { return (TadpoleEntity) super.getHandle(); }
    @Override public int getAge() { return ((TadpoleEntityPaperAccessMixin) getHandle()).meldrite$age(); }
    @Override public void setAge(int age) { requireWritable(); ((TadpoleEntityPaperAccessMixin) getHandle()).meldrite$age(age); }
    @Override public void setAgeLock(boolean lock) { requireWritable(); ((NativeAgeableState) getHandle()).meldrite$setAgeLocked(lock); }
    @Override public boolean getAgeLock() { return ((NativeAgeableState) getHandle()).meldrite$isAgeLocked(); }
}
