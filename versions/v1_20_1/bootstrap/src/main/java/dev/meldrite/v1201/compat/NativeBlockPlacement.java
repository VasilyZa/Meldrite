package dev.meldrite.v1201.compat;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

/** Carries Leaf's NO_PLACE flag through vanilla's chunk-write signature. */
public final class NativeBlockPlacement {
    public static final int NO_PLACE = 1024;
    private record Placement(World world, BlockPos position, BlockState state, boolean enabled) {}
    private static final ThreadLocal<Placement> PLACEMENT = new ThreadLocal<>();

    private NativeBlockPlacement() {}
    public static boolean placementEnabled(World world, BlockPos position, BlockState state) {
        Placement current = PLACEMENT.get();
        return current == null || current.world != world || !current.position.equals(position) || current.state != state || current.enabled;
    }
    public static BlockState setBlockState(WorldChunk chunk, BlockPos position, BlockState state, int flags,
            java.util.function.Supplier<BlockState> write) {
        Placement previous = PLACEMENT.get();
        PLACEMENT.set(new Placement(chunk.getWorld(), position.toImmutable(), state, (flags & NO_PLACE) == 0));
        try { return write.get(); }
        finally {
            if (previous == null) PLACEMENT.remove();
            else PLACEMENT.set(previous);
        }
    }
}
