package org.bukkit.craftbukkit.v1_20_R1.block.impl;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.property.Property;

public final class CraftStairs extends org.bukkit.craftbukkit.v1_20_R1.block.data.type.CraftStairs {
    public CraftStairs(BlockState state) { this(state, null); }
    public CraftStairs(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(requireBlock(state, StairsBlock.class), parsedStates); }
}
