package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.AbstractPiglinEntityPaperAccessMixin;
import net.minecraft.entity.mob.AbstractPiglinEntity;
public class CraftPiglinAbstract extends CraftMonster implements org.bukkit.entity.PiglinAbstract {
    public CraftPiglinAbstract(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AbstractPiglinEntity getHandle() { return (AbstractPiglinEntity) super.getHandle(); }
    private AbstractPiglinEntityPaperAccessMixin access() { return (AbstractPiglinEntityPaperAccessMixin) getHandle(); }
    @Override public boolean isImmuneToZombification() { return access().meldrite$isImmune(); }
    @Override public void setImmuneToZombification(boolean immune) { requireWritable(); getHandle().setImmuneToZombification(immune); }
    @Override public boolean isConverting() { return getHandle().shouldZombify(); }
    @Override public int getConversionTime() { if (!isConverting()) throw new IllegalStateException("Piglin is not converting"); return access().meldrite$timeInOverworld(); }
    @Override public void setConversionTime(int ticks) { requireWritable(); access().meldrite$timeInOverworld(ticks < 0 ? -1 : ticks); if (ticks < 0) getHandle().setImmuneToZombification(false); }
    @Override public boolean isBaby() { return getHandle().isBaby(); }
    @Override public void setBaby(boolean baby) { requireWritable(); getHandle().setBaby(baby); }
    @Override public int getAge() { return isBaby() ? -1 : 0; }
    @Override public void setAge(int age) { setBaby(age < 0); }
    // Piglins do not age naturally or breed; these are the upstream Ageable semantics.
    @Override public void setAgeLock(boolean lock) { requireWritable(); }
    @Override public boolean getAgeLock() { return false; }
    @Override public void setBaby() { setBaby(true); }
    @Override public void setAdult() { setBaby(false); }
    @Override public boolean isAdult() { return !isBaby(); }
    @Override public boolean canBreed() { return false; }
    @Override public void setBreed(boolean breed) { requireWritable(); }
}
