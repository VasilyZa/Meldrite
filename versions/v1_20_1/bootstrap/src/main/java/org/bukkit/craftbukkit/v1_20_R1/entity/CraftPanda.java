package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.PandaEntity;

public class CraftPanda extends CraftAnimals implements org.bukkit.entity.Panda {
    public CraftPanda(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public PandaEntity getHandle() { return (PandaEntity) super.getHandle(); }
    private dev.meldrite.v1201.hook.PandaEntityPaperAccessMixin pandaState() { return (dev.meldrite.v1201.hook.PandaEntityPaperAccessMixin) getHandle(); }
    public static Gene fromNms(PandaEntity.Gene gene) { return Gene.values()[java.util.Objects.requireNonNull(gene, "gene").ordinal()]; }
    public static PandaEntity.Gene toNms(Gene gene) { return PandaEntity.Gene.values()[java.util.Objects.requireNonNull(gene, "gene").ordinal()]; }
    @Override public Gene getMainGene() { return fromNms(getHandle().getMainGene()); }
    @Override public void setMainGene(Gene gene) { requireWritable(); getHandle().setMainGene(toNms(gene)); }
    @Override public Gene getHiddenGene() { return fromNms(getHandle().getHiddenGene()); }
    @Override public void setHiddenGene(Gene gene) { requireWritable(); getHandle().setHiddenGene(toNms(gene)); }
    @Override public boolean isRolling() { return getHandle().isPlaying(); }
    @Override public void setRolling(boolean flag) { requireWritable(); getHandle().setPlaying(flag); }
    @Override public boolean isSneezing() { return getHandle().isSneezing(); }
    @Override public void setSneezing(boolean flag) { requireWritable(); getHandle().setSneezing(flag); }
    @Override public boolean isSitting() { return getHandle().isSitting(); }
    @Override public void setSitting(boolean flag) { requireWritable(); getHandle().setSitting(flag); }
    @Override public boolean isOnBack() { return getHandle().isLyingOnBack(); }
    @Override public void setOnBack(boolean flag) { requireWritable(); getHandle().setLyingOnBack(flag); }
    @Override public boolean isEating() { return getHandle().isEating(); }
    @Override public void setEating(boolean flag) { requireWritable(); getHandle().setEating(flag); }
    @Override public boolean isScared() { return getHandle().isScaredByThunderstorm(); }
    @Override public int getUnhappyTicks() { return getHandle().getAskForBambooTicks(); }
    @Override public void setUnhappyTicks(int ticks) { requireWritable(); getHandle().setAskForBambooTicks(ticks); }
    @Override public int getSneezeTicks() { return getHandle().getSneezeProgress(); }
    @Override public void setSneezeTicks(int ticks) { requireWritable(); getHandle().setSneezeProgress(ticks); }
    @Override public int getEatingTicks() { return pandaState().meldrite$eatingTicks(); }
    @Override public void setEatingTicks(int ticks) { requireWritable(); pandaState().meldrite$eatingTicks(ticks); }
}
