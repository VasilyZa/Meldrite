package org.bukkit.craftbukkit.v1_20_R1.util;

import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.util.shape.VoxelShape;
import org.bukkit.util.BoundingBox;

/** Bukkit projection of a native voxel shape. */
public final class CraftVoxelShape implements org.bukkit.util.VoxelShape {
    private final VoxelShape shape;
    public CraftVoxelShape(VoxelShape shape) { this.shape = java.util.Objects.requireNonNull(shape); }
    @Override public Collection<BoundingBox> getBoundingBoxes() {
        var boxes = new ArrayList<BoundingBox>();
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> boxes.add(new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)));
        return java.util.List.copyOf(boxes);
    }
    @Override public boolean overlaps(BoundingBox other) {
        java.util.Objects.requireNonNull(other, "other");
        return getBoundingBoxes().stream().anyMatch(other::overlaps);
    }
}
