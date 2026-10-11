package org.bukkit.craftbukkit.v1_20_R1.generator;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.bukkit.generator.BiomeParameterPoint;

/** Bukkit view of one native multi-noise biome sample and its sampler bounds. */
public final class CraftBiomeParameterPoint implements BiomeParameterPoint {
    private final double temperature;
    private final double humidity;
    private final double continentalness;
    private final double erosion;
    private final double depth;
    private final double weirdness;
    private final MultiNoiseUtil.MultiNoiseSampler sampler;

    public static BiomeParameterPoint createBiomeParameterPoint(MultiNoiseUtil.MultiNoiseSampler sampler,
                                                                  MultiNoiseUtil.NoiseValuePoint point) {
        return new CraftBiomeParameterPoint(sampler,
                MultiNoiseUtil.toFloat(point.temperatureNoise()),
                MultiNoiseUtil.toFloat(point.humidityNoise()),
                MultiNoiseUtil.toFloat(point.continentalnessNoise()),
                MultiNoiseUtil.toFloat(point.erosionNoise()),
                MultiNoiseUtil.toFloat(point.depth()),
                MultiNoiseUtil.toFloat(point.weirdnessNoise()));
    }

    private CraftBiomeParameterPoint(MultiNoiseUtil.MultiNoiseSampler sampler, double temperature, double humidity,
                                     double continentalness, double erosion, double depth, double weirdness) {
        this.sampler = java.util.Objects.requireNonNull(sampler, "sampler");
        this.temperature = temperature;
        this.humidity = humidity;
        this.continentalness = continentalness;
        this.erosion = erosion;
        this.depth = depth;
        this.weirdness = weirdness;
    }

    @Override public double getTemperature() { return temperature; }
    @Override public double getMaxTemperature() { return sampler.temperature().maxValue(); }
    @Override public double getMinTemperature() { return sampler.temperature().minValue(); }
    @Override public double getHumidity() { return humidity; }
    @Override public double getMaxHumidity() { return sampler.humidity().maxValue(); }
    @Override public double getMinHumidity() { return sampler.humidity().minValue(); }
    @Override public double getContinentalness() { return continentalness; }
    @Override public double getMaxContinentalness() { return sampler.continentalness().maxValue(); }
    @Override public double getMinContinentalness() { return sampler.continentalness().minValue(); }
    @Override public double getErosion() { return erosion; }
    @Override public double getMaxErosion() { return sampler.erosion().maxValue(); }
    @Override public double getMinErosion() { return sampler.erosion().minValue(); }
    @Override public double getDepth() { return depth; }
    @Override public double getMaxDepth() { return sampler.depth().maxValue(); }
    @Override public double getMinDepth() { return sampler.depth().minValue(); }
    @Override public double getWeirdness() { return weirdness; }
    @Override public double getMaxWeirdness() { return sampler.weirdness().maxValue(); }
    @Override public double getMinWeirdness() { return sampler.weirdness().minValue(); }
}
