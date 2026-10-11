package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import org.bukkit.Art;
public class CraftPainting extends CraftHanging implements org.bukkit.entity.Painting {
    public CraftPainting(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.decoration.painting.PaintingEntity getHandle() { return (net.minecraft.entity.decoration.painting.PaintingEntity) super.getHandle(); }
    @Override public Art getArt() { return org.bukkit.craftbukkit.v1_20_R1.CraftArt.NotchToBukkit(getHandle().getVariant()); }
    @Override public boolean setArt(Art art) { return setArt(art, false); }
    @Override public boolean setArt(Art art, boolean force) {
        var variant = org.bukkit.craftbukkit.v1_20_R1.CraftArt.BukkitToNotch(art); delegate.requireWritable(); var entity = getHandle(); var previous = entity.getVariant();
        entity.setVariant(variant); entity.setFacing(entity.facing);
        if (!force && !entity.canStayAttached()) { entity.setVariant(previous); entity.setFacing(entity.facing); return false; } return true;
    }
    @Override public boolean setFacingDirection(org.bukkit.block.BlockFace face, boolean force) { if (!super.setFacingDirection(face, force)) return false; update(); return true; }
}
