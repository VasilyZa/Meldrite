package org.bukkit.craftbukkit.v1_20_R1.generator;

import java.lang.ref.WeakReference;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

/** Mutable generator view over the native chunk being generated. */
public final class CraftChunkData implements ChunkGenerator.ChunkData {
    private final int minHeight;
    private final int maxHeight;
    private final WeakReference<Chunk> weakChunk;

    public CraftChunkData(World world, Chunk chunk) {
        this(Objects.requireNonNull(world, "world").getMinHeight(), world.getMaxHeight(), chunk);
    }

    CraftChunkData(int minHeight, int maxHeight, Chunk chunk) {
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.weakChunk = new WeakReference<>(Objects.requireNonNull(chunk, "chunk"));
    }

    public Chunk getHandle() {
        Chunk chunk = weakChunk.get();
        if (chunk == null) throw new IllegalStateException("Chunk data is no longer available");
        return chunk;
    }

    public void breakLink() { weakChunk.clear(); }
    @Override public int getMinHeight() { return minHeight; }
    @Override public int getMaxHeight() { return maxHeight; }

    private BlockState state(int x, int y, int z) {
        if (x < 0 || x >= 16 || z < 0 || z >= 16 || y < minHeight || y >= maxHeight) {
            return net.minecraft.block.Blocks.AIR.getDefaultState();
        }
        Chunk chunk = getHandle();
        BlockPos pos = new BlockPos((chunk.getPos().x << 4) + x, y, (chunk.getPos().z << 4) + z);
        return chunk.getBlockState(pos);
    }

    @Override public org.bukkit.block.Biome getBiome(int x, int y, int z) {
        RegistryEntry<Biome> entry = getHandle().getBiomeForNoiseGen(x >> 2, y >> 2, z >> 2);
        var key = entry == null ? null : entry.getKey().orElse(null);
        if (key == null) return org.bukkit.block.Biome.CUSTOM;
        org.bukkit.block.Biome result = org.bukkit.Registry.BIOME.get(new org.bukkit.NamespacedKey(key.getValue().getNamespace(), key.getValue().getPath()));
        return result == null ? org.bukkit.block.Biome.CUSTOM : result;
    }

    @Override public void setBlock(int x, int y, int z, Material material) {
        setBlock(x, y, z, Objects.requireNonNull(material, "material").createBlockData());
    }
    @Override public void setBlock(int x, int y, int z, MaterialData material) {
        setBlock(x, y, z, CraftMagicNumbers.getBlock(Objects.requireNonNull(material, "material")));
    }
    @Override public void setBlock(int x, int y, int z, BlockData data) {
        setBlock(x, y, z, dev.meldrite.v1201.compat.NativeBlockData.require(Objects.requireNonNull(data, "data")));
    }
    private void setBlock(int x, int y, int z, BlockState state) {
        if (x < 0 || x >= 16 || z < 0 || z >= 16 || y < minHeight || y >= maxHeight) return;
        Chunk chunk = getHandle();
        BlockPos pos = new BlockPos((chunk.getPos().x << 4) + x, y, (chunk.getPos().z << 4) + z);
        BlockState old = chunk.setBlockState(pos, state, false);
        if (state.getBlock() instanceof BlockEntityProvider provider) {
            BlockEntity entity = provider.createBlockEntity(pos, state);
            if (entity != null) chunk.setBlockEntity(entity);
            else if (old != null && old.hasBlockEntity()) chunk.removeBlockEntity(pos);
        } else if (old != null && old.hasBlockEntity()) {
            chunk.removeBlockEntity(pos);
        }
    }

    @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material) {
        setRegion(xMin, yMin, zMin, xMax, yMax, zMax, Objects.requireNonNull(material, "material").createBlockData());
    }
    @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, MaterialData material) {
        setRegion(xMin, yMin, zMin, xMax, yMax, zMax, CraftBlockData.fromData(CraftMagicNumbers.getBlock(Objects.requireNonNull(material, "material"))));
    }
    @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, BlockData data) {
        BlockState state = dev.meldrite.v1201.compat.NativeBlockData.require(Objects.requireNonNull(data, "data"));
        int fromX = Math.max(0, xMin), toX = Math.min(16, xMax);
        int fromY = Math.max(minHeight, yMin), toY = Math.min(maxHeight, yMax);
        int fromZ = Math.max(0, zMin), toZ = Math.min(16, zMax);
        for (int y = fromY; y < toY; y++) for (int x = fromX; x < toX; x++) for (int z = fromZ; z < toZ; z++) setBlock(x, y, z, state);
    }
    @Override public Material getType(int x, int y, int z) { return CraftMagicNumbers.getMaterial(state(x, y, z).getBlock()); }
    @Override public MaterialData getTypeAndData(int x, int y, int z) { return CraftMagicNumbers.getMaterial(state(x, y, z)); }
    @Override public BlockData getBlockData(int x, int y, int z) { return CraftBlockData.fromData(state(x, y, z)); }
    @Override public byte getData(int x, int y, int z) { return CraftMagicNumbers.toLegacyData(state(x, y, z)); }
}
