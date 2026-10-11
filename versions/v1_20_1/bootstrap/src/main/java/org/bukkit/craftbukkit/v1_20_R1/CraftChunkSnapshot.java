package org.bukkit.craftbukkit.v1_20_R1;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.WorldChunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;

public final class CraftChunkSnapshot implements org.bukkit.ChunkSnapshot {
    private final int x, z, minY, height;
    private final String worldName;
    private final long captureTime;
    private final BlockData[] blocks;
    private final int[] skyLight, blockLight, highest;
    private final Biome[] biomes;
    private final double[] temperatures;
    private final boolean[] emptySections;

    private CraftChunkSnapshot(CraftWorld world, int x, int z, boolean includeBiome, boolean includeBiomeTemp) {
        this.x = x; this.z = z; this.minY = world.getMinHeight(); this.height = world.getMaxHeight() - minY;
        this.worldName = world.getName(); this.captureTime = world.getFullTime();
        int size = 16 * 16 * height;
        this.blocks = new BlockData[size]; this.skyLight = new int[size]; this.blockLight = new int[size];
        this.highest = new int[256]; this.biomes = includeBiome ? new Biome[256 * height] : null;
        this.temperatures = includeBiomeTemp ? new double[256 * height] : null;
        this.emptySections = new boolean[(height + 15) >> 4];
        BlockData air = CraftBlockData.fromData(Blocks.AIR.getDefaultState());
        for (int section = 0; section < emptySections.length; section++) emptySections[section] = true;
        for (int i = 0; i < blocks.length; i++) blocks[i] = air;
        java.util.Arrays.fill(highest, minY);
        if (biomes != null) java.util.Arrays.fill(biomes, Biome.PLAINS);
        if (temperatures != null) java.util.Arrays.fill(temperatures, 0.8D);
    }

    static CraftChunkSnapshot empty(CraftWorld world, int x, int z, boolean includeBiome, boolean includeBiomeTemp) {
        return new CraftChunkSnapshot(world, x, z, includeBiome, includeBiomeTemp);
    }

    CraftChunkSnapshot(CraftWorld world, WorldChunk chunk, boolean includeBiome, boolean includeBiomeTemp) {
        this.x = chunk.getPos().x; this.z = chunk.getPos().z; this.minY = world.getMinHeight(); this.height = world.getMaxHeight() - minY;
        this.worldName = world.getName(); this.captureTime = world.getFullTime();
        this.blocks = new BlockData[16 * 16 * height]; this.skyLight = new int[blocks.length]; this.blockLight = new int[blocks.length];
        this.highest = new int[256]; this.biomes = includeBiome ? new Biome[256 * height] : null;
        this.temperatures = includeBiomeTemp ? new double[256 * height] : null;
        this.emptySections = new boolean[(height + 15) >> 4];
        var nativeWorld = world.getHandle();
        for (int section = 0; section < emptySections.length; section++) {
            int sectionY = minY + (section << 4);
            emptySections[section] = sectionY >= minY && sectionY + 16 <= world.getMaxHeight() && chunk.getSection(section).isEmpty();
        }
        for (int iy = 0; iy < height; iy++) for (int bz = 0; bz < 16; bz++) for (int bx = 0; bx < 16; bx++) {
            int by = minY + iy, index = index(bx, by, bz);
            BlockPos pos = new BlockPos((x << 4) + bx, by, (z << 4) + bz);
            blocks[index] = CraftBlockData.fromData(chunk.getBlockState(pos));
            skyLight[index] = nativeWorld.getLightLevel(LightType.SKY, pos);
            blockLight[index] = nativeWorld.getLightLevel(LightType.BLOCK, pos);
            if (iy == 0 || !blocks[index].getMaterial().isAir()) highest[(bz << 4) | bx] = by;
            if (includeBiome || includeBiomeTemp) {
                var entry = chunk.getBiomeForNoiseGen((x << 2) + (bx >> 2), by >> 2, (z << 2) + (bz >> 2));
                var key = entry.getKey().orElse(null);
                Biome projected = key == null ? Biome.CUSTOM : org.bukkit.Registry.BIOME.get(NamespacedKey.fromString(key.getValue().toString()));
                if (projected == null) projected = Biome.CUSTOM;
                int biomeIndex = ((iy * 16 + bz) << 4) | bx;
                if (biomes != null) biomes[biomeIndex] = projected;
                if (temperatures != null) temperatures[biomeIndex] = entry.value().getTemperature();
            }
        }
        for (int bz = 0; bz < 16; bz++) for (int bx = 0; bx < 16; bx++) {
            highest[(bz << 4) | bx] = chunk.sampleHeightmap(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, bx, bz);
        }
    }

    @Override public int getX() { return x; }
    @Override public int getZ() { return z; }
    @Override public String getWorldName() { return worldName; }
    @Override public Material getBlockType(int x, int y, int z) { return getBlockData(x, y, z).getMaterial(); }
    @Override public BlockData getBlockData(int x, int y, int z) { return blocks[index(x, y, z)].clone(); }
    @Override public int getData(int x, int y, int z) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers.toLegacyData(dev.meldrite.v1201.compat.NativeBlockData.require(getBlockData(x, y, z))) & 0xff; }
    @Override public int getBlockSkyLight(int x, int y, int z) { return skyLight[index(x, y, z)]; }
    @Override public int getBlockEmittedLight(int x, int y, int z) { return blockLight[index(x, y, z)]; }
    @Override public int getHighestBlockYAt(int x, int z) { requireColumn(x, z); return highest[(z << 4) | x]; }
    @Override public Biome getBiome(int x, int z) { return getBiome(x, Math.max(minY, Math.min(minY + height - 1, 63)), z); }
    @Override public Biome getBiome(int x, int y, int z) { requireColumn(x, z); requireY(y); if (biomes == null) throw new IllegalStateException("Biome data was not captured"); return biomes[index(x, y, z)]; }
    @Override public double getRawBiomeTemperature(int x, int z) { return getRawBiomeTemperature(x, Math.max(minY, Math.min(minY + height - 1, 63)), z); }
    @Override public double getRawBiomeTemperature(int x, int y, int z) { requireColumn(x, z); requireY(y); if (temperatures == null) throw new IllegalStateException("Biome temperature data was not captured"); return temperatures[index(x, y, z)]; }
    @Override public long getCaptureFullTime() { return captureTime; }
    @Override public boolean isSectionEmpty(int section) { if (section < 0 || section >= emptySections.length) throw new IllegalArgumentException("Invalid chunk section: " + section); return emptySections[section]; }
    @Override public boolean contains(BlockData data) { java.util.Objects.requireNonNull(data); for (BlockData block : blocks) if (block.matches(data)) return true; return false; }
    @Override public boolean contains(Biome biome) { if (biomes == null) throw new IllegalStateException("Biome data was not captured"); for (Biome value : biomes) if (value == biome) return true; return false; }
    private int index(int bx, int by, int bz) { requireColumn(bx, bz); requireY(by); return (((by - minY) * 16 + bz) << 4) | bx; }
    private void requireY(int y) { if (y < minY || y >= minY + height) throw new IllegalArgumentException("Y outside world height: " + y); }
    private static void requireColumn(int x, int z) { if (x < 0 || x > 15 || z < 0 || z > 15) throw new IllegalArgumentException("Chunk-local X/Z must be in [0, 15]"); }
}
