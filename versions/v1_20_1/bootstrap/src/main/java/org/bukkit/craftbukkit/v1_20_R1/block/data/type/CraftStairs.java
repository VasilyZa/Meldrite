package org.bukkit.craftbukkit.v1_20_R1.block.data.type;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.state.property.Property;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftDirectional;

/** Complete Stairs view using the four properties defined by StairsBlock. */
public abstract class CraftStairs extends CraftDirectional implements Stairs, Bisected {
    public CraftStairs(BlockState state) { super(state); }
    public CraftStairs(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(state, parsedStates); }
    @Override public Shape getShape() { return Shape.valueOf(this.<StairShape>propertyValue("shape").name()); }
    @Override public void setShape(Shape shape) {
        java.util.Objects.requireNonNull(shape, "shape");
        propertyValue("shape", StairShape.valueOf(shape.name()));
    }
    @Override public Half getHalf() { return Half.valueOf(this.<BlockHalf>propertyValue("half").name()); }
    @Override public void setHalf(Half half) {
        java.util.Objects.requireNonNull(half, "half");
        propertyValue("half", BlockHalf.valueOf(half.name()));
    }
    @Override public boolean isWaterlogged() { return propertyValue("waterlogged"); }
    @Override public void setWaterlogged(boolean waterlogged) { propertyValue("waterlogged", waterlogged); }
}
