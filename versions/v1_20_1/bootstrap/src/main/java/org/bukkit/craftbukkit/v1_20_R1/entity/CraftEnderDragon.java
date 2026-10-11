package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseType;

public class CraftEnderDragon extends CraftMob implements org.bukkit.entity.EnderDragon, CraftEnemy {
    public CraftEnderDragon(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public EnderDragonEntity getHandle() { return (EnderDragonEntity) super.getHandle(); }
    @Override public org.bukkit.entity.EnderDragon.Phase getPhase() { return org.bukkit.entity.EnderDragon.Phase.values()[getHandle().getPhaseManager().getCurrent().getType().getTypeId()]; }
    @Override public void setPhase(org.bukkit.entity.EnderDragon.Phase phase) { requireWritable(); getHandle().getPhaseManager().setPhase(PhaseType.getFromId(phase.ordinal())); }
    @Override public org.bukkit.boss.BossBar getBossBar() { var battle = getDragonBattle(); return battle == null ? null : battle.getBossBar(); }
    @Override public org.bukkit.boss.DragonBattle getDragonBattle() { var fight = getHandle().getFight(); return fight == null ? null : new org.bukkit.craftbukkit.v1_20_R1.boss.CraftDragonBattle(fight); }
    @Override public int getDeathAnimationTicks() { return getHandle().ticksSinceDeath; }
    @Override public org.bukkit.Location getPodium() { var p = getHandle().getFightOrigin(); return p == null ? null : new org.bukkit.Location(getWorld(), p.getX(), p.getY(), p.getZ()); }
    @Override public void setPodium(org.bukkit.Location location) { if (location != null && location.getWorld() != getWorld()) throw new IllegalArgumentException("different world"); requireWritable(); getHandle().setFightOrigin(location == null ? null : new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())); }
    @Override public java.util.Set<org.bukkit.entity.ComplexEntityPart> getParts() { var result = new java.util.LinkedHashSet<org.bukkit.entity.ComplexEntityPart>(); for (var part : getHandle().getBodyParts()) result.add((CraftEnderDragonPart) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(part)); return java.util.Collections.unmodifiableSet(result); }
}
