package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.SlimeEntity;

public class CraftSlime extends CraftMob implements org.bukkit.entity.Slime, CraftEnemy {
    private boolean canWander = true;
    public CraftSlime(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SlimeEntity getHandle() { return (SlimeEntity) super.getHandle(); }
    @Override public int getSize() { return getHandle().getSize(); }
    @Override public void setSize(int size) { if (size < 1) throw new IllegalArgumentException("size must be positive"); requireWritable(); getHandle().setSize(size, getHandle().isAlive()); }
    @Override public boolean canWander() { return canWander; }
    @Override public void setWander(boolean value) { requireWritable(); canWander = value; }
}
