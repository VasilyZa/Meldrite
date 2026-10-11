package org.bukkit.craftbukkit.v1_20_R1.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.CraftRegionAccessor;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.util.BoundingBox;

/** Bounded WorldGenRegion view used by Bukkit block populators. */
public final class CraftLimitedRegion extends CraftRegionAccessor implements LimitedRegion {
    private final StructureWorldAccess access;
    private final CraftWorld world;
    private final int centerChunkX;
    private final int centerChunkZ;
    private final int buffer;
    private final BoundingBox region;

    public CraftLimitedRegion(StructureWorldAccess access, ChunkPos center) {
        this(access, center, 16);
    }
    public CraftLimitedRegion(StructureWorldAccess access, ChunkPos center, int buffer) {
        this.access = Objects.requireNonNull(access, "access");
        this.world = (CraftWorld) dev.meldrite.v1201.compat.NativeEntityExtensions.world(access.toServerWorld());
        this.centerChunkX = center.x;
        this.centerChunkZ = center.z;
        this.buffer = Math.max(0, buffer);
        int minX = (center.x << 4) - this.buffer;
        int minZ = (center.z << 4) - this.buffer;
        this.region = new BoundingBox(minX, world.getMinHeight(), minZ, (center.x << 4) + 16 + this.buffer,
                world.getMaxHeight(), (center.z << 4) + 16 + this.buffer);
    }
    public StructureWorldAccess getHandle() { return access; }
    @Override public World getWorld() { return world; }
    @Override public int getBuffer() { return buffer; }
    @Override public boolean isInRegion(Location location) { return isInRegion(location.getBlockX(), location.getBlockY(), location.getBlockZ()); }
    @Override public boolean isInRegion(int x, int y, int z) { return region.contains(x, y, z); }
    private void check(int x, int y, int z) { if (!isInRegion(x, y, z)) throw new IllegalArgumentException("Location is outside the generation region"); }
    @Override public org.bukkit.block.Biome getBiome(int x, int y, int z) { check(x,y,z); return super.getBiome(x,y,z); }
    @Override public org.bukkit.block.Biome getComputedBiome(int x, int y, int z) { check(x,y,z); return super.getComputedBiome(x,y,z); }
    @Override public BlockState getBlockState(int x, int y, int z) { check(x,y,z); return super.getBlockState(x,y,z); }
    @Override public BlockData getBlockData(int x, int y, int z) { check(x,y,z); return super.getBlockData(x,y,z); }
    @Override public Material getType(int x, int y, int z) { check(x,y,z); return super.getType(x,y,z); }
    @Override public void setBlockData(int x, int y, int z, BlockData data) { check(x,y,z); super.setBlockData(x,y,z,data); }
    @Override public void setBlockState(int x, int y, int z, BlockState state) {
        check(x,y,z);
        BlockPos pos = new BlockPos(x,y,z);
        org.bukkit.block.BlockState current = super.getBlockState(x,y,z);
        if (current.getType() != state.getType()) throw new IllegalArgumentException("BlockData does not match the region block type");
        if (!(state instanceof org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockState craft)) {
            throw new IllegalArgumentException("BlockState is not backed by the active CraftBukkit implementation");
        }
        if (access instanceof net.minecraft.world.ChunkRegion region) {
            region.setBlockState(pos, craft.getHandle(), 18, 512);
            if (state instanceof org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockEntityState<?> entityState
                    && access.getBlockEntity(pos) != null) access.getBlockEntity(pos).readNbt(entityState.getSnapshotNBT());
            return;
        }
        state.update(true, false);
    }
    @Override public List<BlockState> getTileEntities() {
        List<BlockState> result = new ArrayList<>();
        for (int x = (int) region.getMinX(); x < (int) region.getMaxX(); x++) for (int y = (int) region.getMinY(); y < (int) region.getMaxY(); y++) for (int z = (int) region.getMinZ(); z < (int) region.getMaxZ(); z++) {
            if (access.getBlockEntity(new BlockPos(x,y,z)) != null) result.add(getBlockState(x,y,z));
        }
        return result;
    }
    @Override public void scheduleBlockUpdate(int x, int y, int z) { check(x,y,z); access.scheduleBlockTick(new BlockPos(x,y,z), access.getBlockState(new BlockPos(x,y,z)).getBlock(), 0); }
    @Override public void scheduleFluidUpdate(int x, int y, int z) { check(x,y,z); access.scheduleFluidTick(new BlockPos(x,y,z), access.getFluidState(new BlockPos(x,y,z)).getFluid(), 0); }
    @Override public int getCenterChunkX() { return centerChunkX; }
    @Override public int getCenterChunkZ() { return centerChunkZ; }
}
