package org.bukkit.craftbukkit.v1_20_R1.block.data.type;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.Property;
import org.bukkit.block.data.type.Slab;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftWaterlogged;

/** Slab type and waterlogging backed by the actual SlabBlock properties. */
public abstract class CraftSlab extends CraftWaterlogged implements Slab {
    public CraftSlab(BlockState state) { super(state); }
    public CraftSlab(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(state, parsedStates); }
    @Override public Type getType() { return Type.valueOf(this.<SlabType>propertyValue("type").name()); }
    @Override public void setType(Type type) {
        java.util.Objects.requireNonNull(type, "type");
        propertyValue("type", SlabType.valueOf(type.name()));
    }
}
