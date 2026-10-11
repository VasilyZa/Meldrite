package org.bukkit.craftbukkit.v1_20_R1.generator;

import java.util.function.Function;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeSource;

/** Stable extension point used by Bukkit's custom generator bridge. */
public abstract class InternalChunkGenerator extends net.minecraft.world.gen.chunk.ChunkGenerator {
    protected InternalChunkGenerator(BiomeSource source, Function<RegistryEntry<Biome>, GenerationSettings> settings) {
        super(source, settings);
    }
}
