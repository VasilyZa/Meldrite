package org.bukkit.craftbukkit.v1_20_R1.generator;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftHeightMap;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.util.RandomSourceWrapper;

/** Delegating native generator with Bukkit's modern generation callbacks. */
public final class CustomChunkGenerator extends InternalChunkGenerator {
    private final ChunkGenerator delegate;
    private final org.bukkit.generator.ChunkGenerator generator;
    private final CraftWorld world;
    private final CraftWorldInfo worldInfo;

    public CustomChunkGenerator(CraftWorld world, ChunkGenerator delegate, org.bukkit.generator.ChunkGenerator generator) {
        super(Objects.requireNonNull(delegate, "delegate").getBiomeSource(), delegate::getGenerationSettings);
        this.world = Objects.requireNonNull(world, "world");
        this.delegate = delegate;
        this.generator = Objects.requireNonNull(generator, "generator");
        this.worldInfo = new CraftWorldInfo(world.getHandle(), world.getEnvironment(), delegate);
    }
    public ChunkGenerator getDelegate() { return delegate; }
    @Override public net.minecraft.world.biome.source.BiomeSource getBiomeSource() { return delegate.getBiomeSource(); }
    @Override public int getMinimumY() { return delegate.getMinimumY(); }
    @Override public int getWorldHeight() { return delegate.getWorldHeight(); }
    @Override public int getSeaLevel() { return delegate.getSeaLevel(); }
    @Override public int getHeight(int x, int z, net.minecraft.world.Heightmap.Type type, net.minecraft.world.HeightLimitView height, NoiseConfig noise) {
        Random random = seeded(x, z, "base-height");
        try { return generator.getBaseHeight(worldInfo, random, x, z, CraftHeightMap.fromNMS(type)); }
        catch (UnsupportedOperationException ignored) { return delegate.getHeight(x, z, type, height, noise); }
    }
    @Override public VerticalBlockSample getColumnSample(int x, int z, net.minecraft.world.HeightLimitView height, NoiseConfig noise) { return delegate.getColumnSample(x, z, height, noise); }
    @Override public void getDebugHudText(List<String> text, NoiseConfig noise, BlockPos pos) { delegate.getDebugHudText(text, noise, pos); }
    @Override protected Codec<? extends ChunkGenerator> getCodec() { return Codec.unit(this); }

    @Override public void carve(ChunkRegion region, long seed, NoiseConfig noise, BiomeAccess biomes, StructureAccessor structures, Chunk chunk, GenerationStep.Carver step) {
        int x = chunk.getPos().x, z = chunk.getPos().z;
        Random random = seeded(x, z, "caves");
        if (generator.shouldGenerateCaves(worldInfo, random, x, z)) delegate.carve(region, seed, noise, biomes, structures, chunk, step);
        random.setSeed(seed ^ (((long) x) * 341873128712L) ^ (((long) z) * 132897987541L));
        generator.generateCaves(worldInfo, random, x, z, new CraftChunkData(world, chunk));
    }
    @Override public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noise, Chunk chunk) {
        int x = chunk.getPos().x, z = chunk.getPos().z;
        Random random = seeded(x, z, "surface");
        if (generator.shouldGenerateSurface(worldInfo, random, x, z)) delegate.buildSurface(region, structures, noise, chunk);
        generator.generateSurface(worldInfo, random, x, z, new CraftChunkData(world, chunk));
        if (generator.shouldGenerateBedrock()) generator.generateBedrock(worldInfo, random, x, z, new CraftChunkData(world, chunk));
    }
    @Override public void populateEntities(ChunkRegion region) {
        if (generator.shouldGenerateMobs(worldInfo, new Random(), region.getCenterPos().x, region.getCenterPos().z)) delegate.populateEntities(region);
    }
    @Override public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noise, StructureAccessor structures, Chunk chunk) {
        int x = chunk.getPos().x, z = chunk.getPos().z;
        CompletableFuture<Chunk> result = generator.shouldGenerateNoise(worldInfo, seeded(x, z, "noise"), x, z)
                ? delegate.populateNoise(executor, blender, noise, structures, chunk) : CompletableFuture.completedFuture(chunk);
        return result.thenApplyAsync(value -> {
            generator.generateNoise(worldInfo, seeded(x, z, "noise-callback"), x, z, new CraftChunkData(world, value));
            return value;
        }, executor);
    }
    @Override public void generateFeatures(StructureWorldAccess access, Chunk chunk, StructureAccessor structures) {
        int x = chunk.getPos().x, z = chunk.getPos().z;
        if (generator.shouldGenerateDecorations(worldInfo, seeded(x, z, "decorations"), x, z)) delegate.generateFeatures(access, chunk, structures);
    }
    private Random seeded(int x, int z, String stage) {
        return new Random(world.getHandle().getSeed() ^ ((long) x * 341873128712L) ^ ((long) z * 132897987541L) ^ stage.hashCode());
    }
    @Deprecated
    private final class CustomBiomeGrid implements org.bukkit.generator.ChunkGenerator.BiomeGrid {
        private final Chunk chunk;
        private CustomBiomeGrid(Chunk chunk) { this.chunk = chunk; }
        @Override public org.bukkit.block.Biome getBiome(int x, int z) { return getBiome(x, 0, z); }
        @Override public org.bukkit.block.Biome getBiome(int x, int y, int z) {
            var entry = chunk.getBiomeForNoiseGen(x >> 2, y >> 2, z >> 2);
            var key = entry == null ? null : entry.getKey().orElse(null);
            if (key == null) return org.bukkit.block.Biome.CUSTOM;
            var value = org.bukkit.Registry.BIOME.get(new org.bukkit.NamespacedKey(key.getValue().getNamespace(), key.getValue().getPath()));
            return value == null ? org.bukkit.block.Biome.CUSTOM : value;
        }
        @Override public void setBiome(int x, int z, org.bukkit.block.Biome biome) {
            for (int y = world.getMinHeight(); y < world.getMaxHeight(); y += 4) setBiome(x, y, z, biome);
        }
        @Override public void setBiome(int x, int y, int z, org.bukkit.block.Biome biome) {
            if (biome == org.bukkit.block.Biome.CUSTOM) throw new IllegalArgumentException("CUSTOM biome cannot be generated");
            var id = new net.minecraft.util.Identifier(biome.getKey().getNamespace(), biome.getKey().getKey());
            var key = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.BIOME, id);
            var entry = world.getHandle().getRegistryManager().get(net.minecraft.registry.RegistryKeys.BIOME).getEntry(key).orElseThrow();
            int section = world.getHandle().getSectionIndex(y);
            if (section < 0 || section >= chunk.getSectionArray().length) return;
            @SuppressWarnings({"rawtypes", "unchecked"}) var container = (net.minecraft.world.chunk.PalettedContainer) chunk.getSection(section).getBiomeContainer();
            container.set((x >> 2) & 3, (y >> 2) & 3, (z >> 2) & 3, entry);
        }
    }
}
