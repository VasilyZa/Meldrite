package org.bukkit.craftbukkit.v1_20_R1.block.data;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;
import org.bukkit.Axis;
import org.bukkit.block.data.Orientable;

/** Axis view backed by a PillarBlock or another real axis property. */
public class CraftOrientable extends CraftBlockData implements Orientable {
    public CraftOrientable(BlockState state) { super(state); }
    public CraftOrientable(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(state, parsedStates); }
    @Override public Axis getAxis() { return Axis.valueOf(this.<Direction.Axis>propertyValue("axis").name()); }
    @Override public void setAxis(Axis axis) {
        java.util.Objects.requireNonNull(axis, "axis");
        Direction.Axis nativeAxis = Direction.Axis.valueOf(axis.name());
        if (!property("axis").getValues().contains(nativeAxis)) throw new IllegalArgumentException("Axis is not supported: " + axis);
        propertyValue("axis", nativeAxis);
    }
    @Override public Set<Axis> getAxes() {
        EnumSet<Axis> axes = EnumSet.noneOf(Axis.class);
        for (Object value : property("axis").getValues()) if (value instanceof Direction.Axis axis) axes.add(Axis.valueOf(axis.name()));
        return java.util.Collections.unmodifiableSet(axes);
    }
}
