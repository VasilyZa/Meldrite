package org.bukkit.craftbukkit.legacy;

import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.material.MaterialData;

/** Legacy material facade backed by the versioned native conversion table. */
@Deprecated
public final class CraftLegacy {
    private CraftLegacy() {}

    public static Material toLegacy(Material material) { return CraftMagicNumbers.INSTANCE.toLegacy(material); }
    public static MaterialData toLegacyData(Material material) {
        if (material == null || material.isLegacy()) return new MaterialData(material == null ? Material.LEGACY_AIR : material);
        return CraftMagicNumbers.getMaterialData(CraftMagicNumbers.getItem(material));
    }
    public static net.minecraft.block.BlockState fromLegacyData(Material material, byte data) {
        return CraftMagicNumbers.getBlock(material, data);
    }
    public static net.minecraft.item.Item fromLegacyData(Material material, short data) {
        return CraftMagicNumbers.getItem(material, data);
    }
    public static byte toLegacyData(net.minecraft.block.BlockState state) { return CraftMagicNumbers.toLegacyData(state); }
    public static Material toLegacyMaterial(net.minecraft.block.BlockState state) { return CraftMagicNumbers.getMaterial(state.getBlock()); }
    public static MaterialData toLegacy(net.minecraft.block.BlockState state) { return CraftMagicNumbers.getMaterial(state); }
    public static Material fromLegacy(Material material) { return CraftMagicNumbers.INSTANCE.fromLegacy(material); }
    public static Material fromLegacy(MaterialData data) { return CraftMagicNumbers.INSTANCE.fromLegacy(data); }
    public static Material fromLegacy(MaterialData data, boolean itemPriority) { return CraftMagicNumbers.INSTANCE.fromLegacy(data, itemPriority); }

    public static Material[] values() {
        Material[] values = Material.values();
        return Arrays.copyOfRange(values, Material.LEGACY_AIR.ordinal(), values.length);
    }
    public static Material valueOf(String name) {
        return Material.valueOf(name.startsWith(Material.LEGACY_PREFIX) ? name : Material.LEGACY_PREFIX + name);
    }
    public static Material getMaterial(String name) {
        return Material.getMaterial(name.startsWith(Material.LEGACY_PREFIX) ? name : Material.LEGACY_PREFIX + name);
    }
    public static Material matchMaterial(String name) {
        return Material.matchMaterial(name.startsWith(Material.LEGACY_PREFIX) ? name : Material.LEGACY_PREFIX + name);
    }
    public static int ordinal(Material material) {
        if (material == null || !material.isLegacy()) throw new IllegalArgumentException("Material must be legacy");
        return material.ordinal() - Material.LEGACY_AIR.ordinal();
    }
    public static String name(Material material) { return material.name().substring(Material.LEGACY_PREFIX.length()); }
    public static String toString(Material material) { return name(material); }
    public static void init() {}
}
