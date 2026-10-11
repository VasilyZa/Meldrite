package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
public class CraftLeash extends CraftHanging implements org.bukkit.entity.LeashHitch {
    public CraftLeash(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.decoration.LeashKnotEntity getHandle() { return (net.minecraft.entity.decoration.LeashKnotEntity) super.getHandle(); }
    @Override public org.bukkit.block.BlockFace getFacing() { return org.bukkit.block.BlockFace.SELF; }
    @Override public boolean setFacingDirection(org.bukkit.block.BlockFace face, boolean force) { if (face != org.bukkit.block.BlockFace.SELF) throw new IllegalArgumentException("Leash hitch has no direction"); delegate.requireWritable(); return force || getHandle().canStayAttached(); }
}
