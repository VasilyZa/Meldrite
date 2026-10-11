package org.bukkit.craftbukkit.v1_20_R1.block.data;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;

/** MultipleFacing maps only boolean face properties actually declared by the native state. */
public class CraftMultipleFacing extends CraftBlockData implements MultipleFacing {
    public CraftMultipleFacing(BlockState state) { super(state); }
    public CraftMultipleFacing(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(state, parsedStates); }
    private BooleanProperty property(BlockFace face) {
        java.util.Objects.requireNonNull(face, "face");
        Direction direction;
        try { direction = Direction.valueOf(face.name()); } catch (IllegalArgumentException failure) { throw new IllegalArgumentException("Non-allowed face " + face, failure); }
        return getState().getProperties().stream().filter(property -> property instanceof BooleanProperty && property.getName().equals(direction.asString()))
                .map(property -> (BooleanProperty) property).findFirst().orElseThrow(() -> new IllegalArgumentException("Non-allowed face " + face));
    }
    @Override public boolean hasFace(BlockFace face) { return get(property(face)); }
    @Override public void setFace(BlockFace face, boolean has) { set(property(face), has); }
    @Override public Set<BlockFace> getFaces() { return faces(false); }
    @Override public Set<BlockFace> getAllowedFaces() { return faces(true); }
    private Set<BlockFace> faces(boolean allowed) {
        EnumSet<BlockFace> faces = EnumSet.noneOf(BlockFace.class);
        for (Direction direction : Direction.values()) {
            for (Property<?> property : getState().getProperties()) {
                if (property instanceof BooleanProperty face && face.getName().equals(direction.asString()) && (allowed || get(face))) {
                    faces.add(BlockFace.valueOf(direction.name()));
                }
            }
        }
        return java.util.Collections.unmodifiableSet(faces);
    }
}
