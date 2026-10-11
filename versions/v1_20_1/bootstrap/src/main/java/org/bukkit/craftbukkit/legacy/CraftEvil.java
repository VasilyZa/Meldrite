package org.bukkit.craftbukkit.legacy;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

/** Deprecated block/item-id entry points retained for legacy plugins. */
@Deprecated
public final class CraftEvil {
    private CraftEvil() {}

    public static int getBlockTypeIdAt(World world, int x, int y, int z) { return getId(world.getBlockAt(x, y, z).getType()); }
    public static int getBlockTypeIdAt(World world, Location location) { return getId(world.getBlockAt(location).getType()); }
    public static Material getType(Block block) { return CraftLegacy.toLegacy(block.getType()); }
    public static Material getType(BlockState state) { return CraftLegacy.toLegacy(state.getType()); }
    public static int getTypeId(Block block) { return getId(block.getType()); }
    public static boolean setTypeId(Block block, int type) { block.setType(getMaterial(type)); return true; }
    public static boolean setTypeId(Block block, int type, boolean applyPhysics) { block.setType(getMaterial(type), applyPhysics); return true; }
    public static boolean setTypeIdAndData(Block block, int type, byte data, boolean applyPhysics) {
        block.setType(getMaterial(type), applyPhysics);
        setData(block, data, applyPhysics);
        return true;
    }
    public static void setData(Block block, byte data) { setData(block, data, true); }
    public static void setData(Block block, byte data, boolean applyPhysics) {
        Material legacy = CraftLegacy.toLegacy(block.getType());
        block.setBlockData(org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData.fromData(
                CraftLegacy.fromLegacyData(legacy, data)), applyPhysics);
    }
    public static int getTypeId(BlockState state) { return getId(state.getType()); }
    public static boolean setTypeId(BlockState state, int type) { state.setType(getMaterial(type)); return true; }
    public static int getTypeId(ItemStack stack) { return getId(stack.getType()); }
    public static void setTypeId(ItemStack stack, int type) { stack.setType(getMaterial(type)); }
    public static Material getMaterial(int id) {
        for (Material material : Material.values()) if (material.isLegacy() && material.getId() == id) return material;
        return null;
    }
    public static int getId(Material material) { return material == null ? 0 : material.getId(); }
}
