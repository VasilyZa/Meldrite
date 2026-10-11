package org.bukkit.craftbukkit.v1_20_R1;

import org.bukkit.Axis;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;

/** Converts Bukkit effect payloads to the 1.20.1 native integer protocol. */
public final class CraftEffect {
    private CraftEffect() { }
    public static <T> int getDataValue(Effect effect, T data) {
        java.util.Objects.requireNonNull(effect, "effect");
        return switch (effect) {
            case PARTICLES_SCULK_CHARGE, VILLAGER_PLANT_GROW, BONE_MEAL_USE -> ((Number) data).intValue();
            case POTION_BREAK -> data instanceof org.bukkit.potion.Potion potion ? potion.toDamageValue() & 0x3F : ((Color) data).asRGB();
            case INSTANT_POTION_BREAK -> ((Color) data).asRGB();
            case RECORD_PLAY -> {
                if (data != Material.AIR && (!(data instanceof Material material) || !material.isRecord())) throw new IllegalArgumentException("Invalid record type: " + data);
                yield data == Material.AIR ? 0 : net.minecraft.registry.Registries.ITEM.getRawId(CraftMagicNumbers.getItem((Material) data));
            }
            case SMOKE -> smoke((BlockFace) data);
            case STEP_SOUND, PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE -> data instanceof Material material
                    ? net.minecraft.registry.Registries.BLOCK.getRawId(CraftMagicNumbers.getBlock(material))
                    : net.minecraft.registry.Registries.BLOCK.getRawId(((org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData) data).getState().getBlock());
            case COMPOSTER_FILL_ATTEMPT -> (Boolean) data ? 1 : 0;
            case ELECTRIC_SPARK -> data == null ? -1 : ((Axis) data).ordinal();
            default -> 0;
        };
    }
    private static int smoke(BlockFace face) {
        return switch (java.util.Objects.requireNonNull(face, "face")) {
            case DOWN, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST, SELF -> 0;
            case UP -> 1; case NORTH -> 2; case SOUTH -> 3; case WEST -> 4; case EAST -> 5;
            default -> throw new IllegalArgumentException("Bad smoke direction: " + face);
        };
    }
}
