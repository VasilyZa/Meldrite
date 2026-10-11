package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.v1_20_R1.CraftRaid;

public abstract class CraftRaider extends CraftMonster implements org.bukkit.entity.Raider {
    public CraftRaider(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public RaiderEntity getHandle() { return (RaiderEntity) super.getHandle(); }
    @Override public org.bukkit.Raid getRaid() { var raid = getHandle().getRaid(); return raid == null ? null : new CraftRaid(raid); }
    @Override public void setRaid(org.bukkit.Raid raid) { if (raid != null && !(raid instanceof CraftRaid)) throw new IllegalArgumentException("Raid must be native-backed"); requireWritable(); getHandle().setRaid(raid == null ? null : ((CraftRaid) raid).getHandle()); }
    @Override public int getWave() { return getHandle().getWave(); }
    @Override public void setWave(int wave) { if (wave < 0) throw new IllegalArgumentException("wave < 0"); requireWritable(); getHandle().setWave(wave); }
    @Override public org.bukkit.block.Block getPatrolTarget() { var p = getHandle().getPatrolTarget(); return p == null ? null : getWorld().getBlockAt(p.getX(), p.getY(), p.getZ()); }
    @Override public void setPatrolTarget(org.bukkit.block.Block block) { if (block != null && !getWorld().equals(block.getWorld())) throw new IllegalArgumentException("Patrol target must be in the same world"); requireWritable(); getHandle().setPatrolTarget(block == null ? null : new BlockPos(block.getX(), block.getY(), block.getZ())); }
    @Override public boolean isPatrolLeader() { return getHandle().isPatrolLeader(); }
    @Override public void setPatrolLeader(boolean value) { requireWritable(); getHandle().setPatrolLeader(value); }
    @Override public boolean isCanJoinRaid() { return getHandle().canJoinRaid(); }
    @Override public void setCanJoinRaid(boolean value) { requireWritable(); getHandle().setAbleToJoinRaid(value); }
    @Override public int getTicksOutsideRaid() { return getHandle().getOutOfRaidCounter(); }
    @Override public void setTicksOutsideRaid(int ticks) { if (ticks < 0) throw new IllegalArgumentException("ticks < 0"); requireWritable(); getHandle().setOutOfRaidCounter(ticks); }
    @Override public boolean isCelebrating() { return getHandle().isCelebrating(); }
    @Override public void setCelebrating(boolean value) { requireWritable(); getHandle().setCelebrating(value); }
    @Override public org.bukkit.Sound getCelebrationSound() { return org.bukkit.craftbukkit.v1_20_R1.CraftSound.getBukkit(getHandle().getCelebratingSound()); }
}
