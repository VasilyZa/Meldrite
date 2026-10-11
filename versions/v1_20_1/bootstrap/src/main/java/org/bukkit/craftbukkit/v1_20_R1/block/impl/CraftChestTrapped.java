package org.bukkit.craftbukkit.v1_20_R1.block.impl;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.state.property.Property;

public final class CraftChestTrapped extends CraftChest {
    public CraftChestTrapped(BlockState state) { this(state, null); }
    public CraftChestTrapped(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(requireBlock(state, TrappedChestBlock.class), parsedStates); }
}
