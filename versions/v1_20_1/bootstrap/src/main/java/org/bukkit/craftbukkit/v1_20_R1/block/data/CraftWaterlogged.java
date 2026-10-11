package org.bukkit.craftbukkit.v1_20_R1.block.data;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.bukkit.block.data.Waterlogged;

/** Waterlogged view backed by the block state's real boolean property. */
public class CraftWaterlogged extends CraftBlockData implements Waterlogged {
    public CraftWaterlogged(BlockState state) { super(state); }
    public CraftWaterlogged(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(state, parsedStates); }
    @Override public boolean isWaterlogged() { return propertyValue("waterlogged"); }
    @Override public void setWaterlogged(boolean waterlogged) { propertyValue("waterlogged", waterlogged); }
}
