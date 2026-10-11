package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.HoglinEntityPaperAccessMixin;
import net.minecraft.entity.mob.HoglinEntity;

public final class CraftHoglin extends CraftAnimals implements org.bukkit.entity.Hoglin, org.bukkit.entity.Enemy {
    public CraftHoglin(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public HoglinEntity getHandle() { return (HoglinEntity) super.getHandle(); }
    private HoglinEntityPaperAccessMixin access() { return (HoglinEntityPaperAccessMixin) (Object) getHandle(); }
    @Override public boolean isImmuneToZombification() { return access().meldrite$isImmuneToZombification(); }
    @Override public void setImmuneToZombification(boolean immune) { requireWritable(); getHandle().setImmuneToZombification(immune); }
    @Override public boolean isAbleToBeHunted() { return getHandle().canBeHunted(); }
    @Override public void setIsAbleToBeHunted(boolean hunted) { requireWritable(); access().meldrite$setCannotBeHunted(!hunted); }
    @Override public int getConversionTime() {
        if (!isConverting()) throw new IllegalStateException("Hoglin is not converting");
        return access().meldrite$getTimeInOverworld();
    }
    @Override public void setConversionTime(int ticks) {
        requireWritable();
        access().meldrite$setTimeInOverworld(ticks);
        if (ticks < 0) getHandle().setImmuneToZombification(false);
    }
    @Override public boolean isConverting() { return getHandle().canConvert() && access().meldrite$getTimeInOverworld() >= 0; }
}
