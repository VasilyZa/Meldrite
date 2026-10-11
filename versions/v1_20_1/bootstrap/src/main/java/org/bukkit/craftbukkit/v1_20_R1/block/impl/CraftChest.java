package org.bukkit.craftbukkit.v1_20_R1.block.impl;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.property.Property;

public class CraftChest extends org.bukkit.craftbukkit.v1_20_R1.block.data.type.CraftChest {
    public CraftChest(BlockState state) { this(state, null); }
    public CraftChest(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(requireBlock(state, ChestBlock.class), parsedStates); }
}
