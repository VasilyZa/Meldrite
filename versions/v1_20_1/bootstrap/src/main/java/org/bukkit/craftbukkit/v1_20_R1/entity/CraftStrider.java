package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.SaddledComponentPaperAccessMixin;
import net.minecraft.entity.passive.StriderEntity;
public class CraftStrider extends CraftAnimals implements org.bukkit.entity.Strider {
    public CraftStrider(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public StriderEntity getHandle() { return (StriderEntity) super.getHandle(); }
    private net.minecraft.entity.SaddledComponent steering() { return ((dev.meldrite.v1201.hook.StriderEntityPaperAccessMixin) getHandle()).meldrite$steering(); }
    private SaddledComponentPaperAccessMixin boost() { return (SaddledComponentPaperAccessMixin) steering(); }
    @Override public boolean isShivering() { return getHandle().isCold(); }
    @Override public void setShivering(boolean value) { requireWritable(); getHandle().setCold(value); }
    @Override public boolean hasSaddle() { return getHandle().isSaddled(); }
    @Override public void setSaddle(boolean value) { requireWritable(); steering().setSaddled(value); }
    @Override public int getBoostTicks() { return boost().meldrite$isBoosting() ? boost().meldrite$maximumBoostTime() : 0; }
    @Override public void setBoostTicks(int ticks) { if (ticks < 0) throw new IllegalArgumentException("Boost ticks must be nonnegative"); requireWritable(); var state = boost(); state.meldrite$setBoosting(true); state.meldrite$currentBoostTime(0); state.meldrite$tracker().set(state.meldrite$boostTime(), ticks); }
    @Override public int getCurrentBoostTicks() { return boost().meldrite$isBoosting() ? boost().meldrite$currentBoostTime() : 0; }
    @Override public void setCurrentBoostTicks(int ticks) { requireWritable(); var state = boost(); if (!state.meldrite$isBoosting()) return; if (ticks < 0 || ticks > state.meldrite$maximumBoostTime()) throw new IllegalArgumentException("Current boost ticks outside boost duration"); state.meldrite$currentBoostTime(ticks); }
    @Override public org.bukkit.Material getSteerMaterial() { return org.bukkit.Material.WARPED_FUNGUS_ON_A_STICK; }
}
