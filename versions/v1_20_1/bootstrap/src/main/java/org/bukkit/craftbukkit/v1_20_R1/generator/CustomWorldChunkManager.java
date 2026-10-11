package org.bukkit.craftbukkit.v1_20_R1.generator;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

/** Native biome source that asks a Bukkit BiomeProvider for every 4x4x4 sample. */
public final class CustomWorldChunkManager extends BiomeSource {
    private final WorldInfo worldInfo;
    private final BiomeProvider provider;
    private final Registry<Biome> registry;

    public CustomWorldChunkManager(WorldInfo worldInfo, BiomeProvider provider, Registry<Biome> registry) {
        this.worldInfo = Objects.requireNonNull(worldInfo, "worldInfo");
        this.provider = Objects.requireNonNull(provider, "provider");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override protected Codec<? extends BiomeSource> getCodec() { throw new UnsupportedOperationException("Custom biome sources are runtime-only"); }
    @Override protected Stream<RegistryEntry<Biome>> biomeStream() {
        return provider.getBiomes(worldInfo).stream().map(this::nativeBiome).filter(Objects::nonNull);
    }
    @Override public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler sampler) {
        var point = CraftBiomeParameterPoint.createBiomeParameterPoint(sampler, sampler.sample(x, y, z));
        org.bukkit.block.Biome biome = provider.getBiome(worldInfo, x << 2, y << 2, z << 2, point);
        return nativeBiome(biome);
    }
    private RegistryEntry<Biome> nativeBiome(org.bukkit.block.Biome biome) {
        if (biome == null || biome == org.bukkit.block.Biome.CUSTOM) throw new IllegalArgumentException("BiomeProvider returned CUSTOM/null");
        var key = RegistryKey.of(RegistryKeys.BIOME, new Identifier(biome.getKey().getNamespace(), biome.getKey().getKey()));
        return registry.getEntry(key).orElseThrow(() -> new IllegalArgumentException("Unknown native biome: " + biome.getKey()));
    }
}
