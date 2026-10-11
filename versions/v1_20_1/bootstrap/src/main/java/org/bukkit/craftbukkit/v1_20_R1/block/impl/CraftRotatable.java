package org.bukkit.craftbukkit.v1_20_R1.block.impl;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.state.property.Property;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftOrientable;

public final class CraftRotatable extends CraftOrientable {
    public CraftRotatable(BlockState state) { this(state, null); }
    public CraftRotatable(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(requireBlock(state, PillarBlock.class), parsedStates); }
}
