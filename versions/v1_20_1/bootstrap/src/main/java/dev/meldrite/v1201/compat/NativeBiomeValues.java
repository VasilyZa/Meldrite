package dev.meldrite.v1201.compat;

import net.minecraft.world.biome.Biome;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** Reads private native biome values without substituting a Bukkit-side constant. */
public final class NativeBiomeValues {
    private static final Access ACCESS = discover();

    private NativeBiomeValues() {}

    public static float downfall(Biome biome) {
        try {
            Object weather = ACCESS.weather.get(biome);
            return ACCESS.downfall.getFloat(weather);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Native Biome weather access failed", exception);
        }
    }

    private static Access discover() {
        try {
            Field weather = null;
            for (Field candidate : Biome.class.getDeclaredFields()) {
                if (Modifier.isStatic(candidate.getModifiers())) continue;
                if (!"weather".equals(candidate.getName()) && !"field_26393".equals(candidate.getName())) continue;
                if (!candidate.trySetAccessible()) throw new IllegalStateException("Native Biome weather field is inaccessible");
                weather = candidate;
                break;
            }
            if (weather == null) throw new IllegalStateException("Native Biome weather field was not found");
            Field downfall = null;
            for (Field candidate : weather.getType().getDeclaredFields()) {
                if (candidate.getType() != float.class) continue;
                if (!"downfall".equals(candidate.getName()) && !"comp_846".equals(candidate.getName())) continue;
                if (!candidate.trySetAccessible()) throw new IllegalStateException("Native Biome downfall field is inaccessible");
                downfall = candidate;
                break;
            }
            if (downfall == null) throw new IllegalStateException("Native Biome downfall field was not found");
            return new Access(weather, downfall);
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    private record Access(Field weather, Field downfall) {}
}
