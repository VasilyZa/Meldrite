package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.util.math.Direction;
import org.bukkit.block.BlockFace;
public class CraftHanging extends CraftEntity implements org.bukkit.entity.Hanging {
    public CraftHanging(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AbstractDecorationEntity getHandle() { return (AbstractDecorationEntity) super.getHandle(); }
    @Override public BlockFace getAttachedFace() { return getFacing().getOppositeFace(); }
    @Override public BlockFace getFacing() { var facing = getHandle().facing; return facing == null ? BlockFace.SELF : BlockFace.valueOf(facing.name()); }
    @Override public void setFacingDirection(BlockFace face) { setFacingDirection(face, false); }
    @Override public boolean setFacingDirection(BlockFace face, boolean force) {
        Direction direction = switch (java.util.Objects.requireNonNull(face, "face")) { case SOUTH -> Direction.SOUTH; case WEST -> Direction.WEST; case NORTH -> Direction.NORTH; case EAST -> Direction.EAST; default -> throw new IllegalArgumentException("Invalid hanging face: " + face); };
        return setDirection(direction, force);
    }
    protected boolean setDirection(Direction direction, boolean force) {
        delegate.requireWritable(); var entity = getHandle(); var old = entity.facing; entity.setFacing(direction);
        if (!force && !entity.canStayAttached()) { entity.setFacing(old); return false; }
        return true;
    }
    protected void update() {
        var entity = getHandle(); if (!(entity.getWorld() instanceof net.minecraft.server.world.ServerWorld world)) return;
        var manager = world.getChunkManager(); manager.sendToOtherNearbyPlayers(entity, new net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket(entity.getId()));
        manager.sendToOtherNearbyPlayers(entity, entity.createSpawnPacket()); var data = entity.getDataTracker().getChangedEntries();
        if (data != null) manager.sendToOtherNearbyPlayers(entity, new net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket(entity.getId(), data));
    }
}
