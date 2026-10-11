package org.bukkit.craftbukkit.v1_20_R1.block.impl;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.state.property.Property;

public final class CraftStepAbstract extends org.bukkit.craftbukkit.v1_20_R1.block.data.type.CraftSlab {
    public CraftStepAbstract(BlockState state) { this(state, null); }
    public CraftStepAbstract(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(requireBlock(state, SlabBlock.class), parsedStates); }
}
