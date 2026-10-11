package dev.meldrite.v1201.compat;

/** Native CraftBukkit-compatible beehive capacity state. */
public interface NativeBeehiveCapacity {
    int meldrite$getMaxBees();
    void meldrite$setMaxBees(int maxBees);
    java.util.List<net.minecraft.entity.Entity> meldrite$releaseBees(net.minecraft.server.world.ServerWorld world,
            net.minecraft.block.BlockState state, boolean force);
}
