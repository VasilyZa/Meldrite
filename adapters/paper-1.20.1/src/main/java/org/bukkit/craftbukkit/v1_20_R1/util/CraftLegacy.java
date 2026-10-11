package org.bukkit.craftbukkit.v1_20_R1.util;

import java.util.Arrays;
import org.bukkit.Material;

public final class CraftLegacy {
    private static final int[] LEGACY_ORDINALS = Arrays.stream(Material.values()).filter(Material::isLegacy)
            .mapToInt(Material::ordinal).toArray();
    private CraftLegacy() { }
    public static Material[] modern_values() {
        return Arrays.stream(Material.values()).filter(material -> !material.isLegacy()).toArray(Material[]::new);
    }
    public static int modern_ordinal(Material material) {
        if (material.isLegacy()) throw new NoSuchFieldError("Legacy field ordinal: " + material);
        int ordinal = material.ordinal();
        for (int legacy : LEGACY_ORDINALS) {
            if (legacy >= material.ordinal()) break;
            ordinal--;
        }
        return ordinal;
    }
}
