package org.bukkit.craftbukkit.v1_20_R1.block.data;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

/** Directional view backed by the actual native direction property. */
public class CraftDirectional extends CraftBlockData implements Directional {
    public CraftDirectional(BlockState state) { super(state); }
    public CraftDirectional(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(state, parsedStates); }
    @Override public BlockFace getFacing() { return BlockFace.valueOf(this.<Direction>propertyValue("facing").name()); }
    @Override public void setFacing(BlockFace facing) {
        java.util.Objects.requireNonNull(facing, "facing");
        Direction direction;
        try { direction = Direction.valueOf(facing.name()); }
        catch (IllegalArgumentException failure) { throw new IllegalArgumentException("Not a native direction: " + facing, failure); }
        if (!property("facing").getValues().contains(direction)) throw new IllegalArgumentException("Direction is not supported: " + facing);
        propertyValue("facing", direction);
    }
    @Override public Set<BlockFace> getFaces() {
        EnumSet<BlockFace> faces = EnumSet.noneOf(BlockFace.class);
        for (Object value : property("facing").getValues()) {
            if (value instanceof Direction direction) faces.add(BlockFace.valueOf(direction.name()));
        }
        return java.util.Collections.unmodifiableSet(faces);
    }
    public static final class Waterlogged extends CraftDirectional implements org.bukkit.block.data.Waterlogged {
        public Waterlogged(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(state, parsedStates); }
        @Override public boolean isWaterlogged() { return propertyValue("waterlogged"); }
        @Override public void setWaterlogged(boolean waterlogged) { propertyValue("waterlogged", waterlogged); }
    }
}
