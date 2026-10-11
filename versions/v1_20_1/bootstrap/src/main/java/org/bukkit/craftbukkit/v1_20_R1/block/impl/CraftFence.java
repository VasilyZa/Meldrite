package org.bukkit.craftbukkit.v1_20_R1.block.impl;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.state.property.Property;
import org.bukkit.block.data.type.Fence;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftMultipleFacing;

public final class CraftFence extends CraftMultipleFacing implements Fence {
    public CraftFence(BlockState state) { this(state, null); }
    public CraftFence(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(requireBlock(state, FenceBlock.class), parsedStates); }
    @Override public boolean isWaterlogged() { return propertyValue("waterlogged"); }
    @Override public void setWaterlogged(boolean waterlogged) { propertyValue("waterlogged", waterlogged); }
}
