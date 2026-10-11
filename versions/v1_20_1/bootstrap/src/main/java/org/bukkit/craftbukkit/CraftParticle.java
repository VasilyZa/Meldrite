package org.bukkit.craftbukkit;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.bukkit.Particle;

/** Legacy package entry point retained for plugins compiled against CraftBukkit. */
public final class CraftParticle {
    private CraftParticle() {}

    public static ParticleEffect toNMS(Particle particle) {
        return org.bukkit.craftbukkit.v1_20_R1.CraftParticle.toNMS(particle);
    }

    public static <T> ParticleEffect toNMS(Particle particle, T data) {
        return org.bukkit.craftbukkit.v1_20_R1.CraftParticle.toNMS(particle, data);
    }

    public static Particle toBukkit(ParticleEffect effect) {
        return org.bukkit.craftbukkit.v1_20_R1.CraftParticle.toBukkit(effect);
    }

    public static Particle toBukkit(ParticleType<?> type) {
        return org.bukkit.craftbukkit.v1_20_R1.CraftParticle.toBukkit(type);
    }
}
