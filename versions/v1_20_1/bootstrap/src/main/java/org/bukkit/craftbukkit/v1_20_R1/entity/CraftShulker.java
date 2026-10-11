package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.ShulkerEntityPaperAccessMixin;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.Direction;
import org.bukkit.DyeColor;
import org.bukkit.block.BlockFace;

public class CraftShulker extends CraftGolem implements org.bukkit.entity.Shulker {
    public CraftShulker(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ShulkerEntity getHandle() { return (ShulkerEntity) super.getHandle(); }
    @Override public DyeColor getColor() { return getHandle().getVariant().map(color -> DyeColor.getByWoolData((byte) color.getId())).orElse(null); }
    @Override public void setColor(DyeColor color) { requireWritable(); getHandle().setVariant(java.util.Optional.ofNullable(color).map(value -> net.minecraft.util.DyeColor.byId(value.getWoolData()))); }
    @Override public float getPeek() { return ((ShulkerEntityPaperAccessMixin) getHandle()).meldrite$getPeekAmount() / 100.0f; }
    @Override public void setPeek(float value) { if (!Float.isFinite(value) || value < 0 || value > 1) throw new IllegalArgumentException("Peek outside [0,1]"); requireWritable(); ((ShulkerEntityPaperAccessMixin) getHandle()).meldrite$setPeekAmount((int) (value * 100)); }
    @Override public BlockFace getAttachedFace() { return BlockFace.valueOf(getHandle().getAttachedFace().name()); }
    @Override public void setAttachedFace(BlockFace face) { java.util.Objects.requireNonNull(face); if (!face.isCartesian()) throw new IllegalArgumentException("Noncartesian attached face"); requireWritable(); ((ShulkerEntityPaperAccessMixin) getHandle()).meldrite$setAttachedFace(Direction.valueOf(face.name())); }
}
