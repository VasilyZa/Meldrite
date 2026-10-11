package org.bukkit.craftbukkit.util;

import net.minecraft.entity.SpawnGroup;
import org.bukkit.entity.SpawnCategory;

/** Lossless Bukkit/Yarn mapping used by spawn-limit and tick-limit code. */
public final class CraftSpawnCategory {
    private CraftSpawnCategory() {}
    public static boolean isValidForLimits(SpawnCategory category) { return category != null && category != SpawnCategory.MISC; }
    public static String getConfigNameSpawnLimit(SpawnCategory category) {
        return switch (category) {
            case MONSTER -> "spawn-limits.monsters"; case ANIMAL -> "spawn-limits.animals"; case WATER_ANIMAL -> "spawn-limits.water-animals";
            case WATER_AMBIENT -> "spawn-limits.water-ambient"; case WATER_UNDERGROUND_CREATURE -> "spawn-limits.water-underground-creature";
            case AMBIENT -> "spawn-limits.ambient"; case AXOLOTL -> "spawn-limits.axolotls"; default -> throw new IllegalArgumentException("No spawn limit for " + category);
        };
    }
    public static String getConfigNameTicksPerSpawn(SpawnCategory category) {
        return switch (category) {
            case MONSTER -> "ticks-per.monster-spawns"; case ANIMAL -> "ticks-per.animal-spawns"; case WATER_ANIMAL -> "ticks-per.water-spawns";
            case WATER_AMBIENT -> "ticks-per.water-ambient-spawns"; case WATER_UNDERGROUND_CREATURE -> "ticks-per.water-underground-creature-spawns";
            case AMBIENT -> "ticks-per.ambient-spawns"; case AXOLOTL -> "ticks-per.axolotl-spawns"; default -> throw new IllegalArgumentException("No spawn tick setting for " + category);
        };
    }
    public static long getDefaultTicksPerSpawn(SpawnCategory category) { return category == SpawnCategory.ANIMAL ? 400L : 1L; }
    public static SpawnCategory toBukkit(SpawnGroup category) {
        return switch (category) {
            case MONSTER -> SpawnCategory.MONSTER; case CREATURE -> SpawnCategory.ANIMAL; case AMBIENT -> SpawnCategory.AMBIENT; case AXOLOTLS -> SpawnCategory.AXOLOTL;
            case WATER_CREATURE -> SpawnCategory.WATER_ANIMAL; case WATER_AMBIENT -> SpawnCategory.WATER_AMBIENT; case UNDERGROUND_WATER_CREATURE -> SpawnCategory.WATER_UNDERGROUND_CREATURE; case MISC -> SpawnCategory.MISC;
        };
    }
    public static SpawnGroup toNMS(SpawnCategory category) {
        return switch (category) {
            case MONSTER -> SpawnGroup.MONSTER; case ANIMAL -> SpawnGroup.CREATURE; case AMBIENT -> SpawnGroup.AMBIENT; case AXOLOTL -> SpawnGroup.AXOLOTLS;
            case WATER_ANIMAL -> SpawnGroup.WATER_CREATURE; case WATER_AMBIENT -> SpawnGroup.WATER_AMBIENT; case WATER_UNDERGROUND_CREATURE -> SpawnGroup.UNDERGROUND_WATER_CREATURE; case MISC -> SpawnGroup.MISC;
        };
    }
}
