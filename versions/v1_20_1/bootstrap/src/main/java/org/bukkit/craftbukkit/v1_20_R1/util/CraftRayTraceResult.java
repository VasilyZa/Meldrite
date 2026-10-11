package org.bukkit.craftbukkit.v1_20_R1.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/** Converts native ray-trace results without exposing intermediary classes to plugins. */
public final class CraftRayTraceResult {
    private CraftRayTraceResult() {}

    public static RayTraceResult fromNMS(World world, HitResult hit) {
        if (hit == null || hit.getType() == HitResult.Type.MISS) return null;
        Vec3d position = hit.getPos();
        Vector vector = new Vector(position.x, position.y, position.z);
        if (hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hit).getEntity();
            return new RayTraceResult(vector, dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity), null);
        }
        if (hit instanceof BlockHitResult blockHit) {
            BlockFace face = CraftBlock.notchToBlockFace(blockHit.getSide());
            return new RayTraceResult(vector, world == null ? null : world.getBlockAt(blockHit.getBlockPos().getX(), blockHit.getBlockPos().getY(), blockHit.getBlockPos().getZ()), face);
        }
        return new RayTraceResult(vector);
    }
}
