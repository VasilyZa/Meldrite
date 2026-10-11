package org.bukkit.craftbukkit.v1_20_R1.generator;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

/** WorldInfo backed by a live native ServerWorld during terrain generation. */
public final class CraftWorldInfo implements WorldInfo {
    private final ServerWorld world;
    private final ChunkGenerator generator;
    private final String name;
    private final UUID uid;
    private final World.Environment environment;

    public CraftWorldInfo(ServerWorld world, World.Environment environment) {
        this(world, environment, world.getChunkManager().getChunkGenerator());
    }

    public CraftWorldInfo(ServerWorld world, World.Environment environment, ChunkGenerator generator) {
        this.world = Objects.requireNonNull(world, "world");
        this.generator = Objects.requireNonNull(generator, "generator");
        this.name = world.getLevelProperties() instanceof net.minecraft.world.level.ServerWorldProperties properties
                ? properties.getLevelName() : world.getRegistryKey().getValue().toString();
        org.bukkit.craftbukkit.v1_20_R1.CraftWorld bukkitWorld = dev.meldrite.v1201.compat.NativeEntityExtensions.world(world);
        this.uid = bukkitWorld.getUID();
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    public ServerWorld getHandle() { return world; }
    public ChunkGenerator getChunkGenerator() { return generator; }
    @Override public String getName() { return name; }
    @Override public UUID getUID() { return uid; }
    @Override public World.Environment getEnvironment() { return environment; }
    @Override public long getSeed() { return world.getSeed(); }
    @Override public int getMinHeight() { return world.getBottomY(); }
    @Override public int getMaxHeight() { return world.getDimension().minY() + world.getDimension().height(); }

    @Override public BiomeProvider vanillaBiomeProvider() {
        BiomeSource source = generator.getBiomeSource();
        List<org.bukkit.block.Biome> possible = source.getBiomes().stream().map(CraftWorldInfo::bukkitBiome).filter(Objects::nonNull).toList();
        return new BiomeProvider() {
            @Override public org.bukkit.block.Biome getBiome(WorldInfo ignored, int x, int y, int z) {
                return bukkitBiome(source.getBiome(x >> 2, y >> 2, z >> 2, world.getChunkManager().getNoiseConfig().getMultiNoiseSampler()));
            }
            @Override public List<org.bukkit.block.Biome> getBiomes(WorldInfo ignored) { return possible; }
        };
    }

    private static org.bukkit.block.Biome bukkitBiome(RegistryEntry<Biome> entry) {
        if (entry == null || entry.getKey().isEmpty()) return org.bukkit.block.Biome.CUSTOM;
        var id = entry.getKey().get().getValue();
        org.bukkit.block.Biome value = org.bukkit.Registry.BIOME.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
        return value == null ? org.bukkit.block.Biome.CUSTOM : value;
    }
}
