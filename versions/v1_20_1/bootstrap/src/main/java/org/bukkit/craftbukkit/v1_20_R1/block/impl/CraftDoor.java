package org.bukkit.craftbukkit.v1_20_R1.block.impl;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.state.property.Property;

public final class CraftDoor extends org.bukkit.craftbukkit.v1_20_R1.block.data.type.CraftDoor {
    public CraftDoor(BlockState state) { this(state, null); }
    public CraftDoor(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(requireBlock(state, DoorBlock.class), parsedStates); }
}
