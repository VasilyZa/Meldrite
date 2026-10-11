package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.BlockDisplayEntityPaperAccessMixin;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
public class CraftBlockDisplay extends CraftDisplay implements org.bukkit.entity.BlockDisplay {
    public CraftBlockDisplay(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity getHandle() { return (net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity) super.getHandle(); }
    @Override public org.bukkit.block.data.BlockData getBlock() { return CraftBlockData.fromData(((BlockDisplayEntityPaperAccessMixin) getHandle()).meldrite$block()); }
    @Override public void setBlock(org.bukkit.block.data.BlockData value) { java.util.Objects.requireNonNull(value, "block"); delegate.requireWritable(); ((BlockDisplayEntityPaperAccessMixin) getHandle()).meldrite$block(((CraftBlockData) value).getState()); }
}
