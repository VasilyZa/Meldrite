package org.bukkit.craftbukkit.inventory.util;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Tile converter family retained for plugins that inspect CraftBukkit's nested types. */
public abstract class CraftTileInventoryConverter implements CraftInventoryCreator.InventoryConverter {
    protected abstract InventoryType inventoryType();
    protected Inventory createInventoryInternal(InventoryHolder holder, InventoryType type, String title) { return new CraftCustomInventoryConverter().createInventory(holder, type, title); }
    @Override public Inventory createInventory(InventoryHolder holder, InventoryType type) { return createInventoryInternal(holder, type, null); }
    @Override public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) { return createInventoryInternal(holder, type, title); }
    @Override public Inventory createInventory(InventoryHolder holder, InventoryType type, net.kyori.adventure.text.Component title) { return new CraftCustomInventoryConverter().createInventory(holder, type, title); }
    public Inventory getInventory() { return createInventoryInternal(null, inventoryType(), null); }
    public static class Furnace extends CraftTileInventoryConverter { @Override protected InventoryType inventoryType() { return InventoryType.FURNACE; } }
    public static abstract class AbstractFurnaceInventoryConverter extends CraftTileInventoryConverter { }
    public static class BlastFurnace extends AbstractFurnaceInventoryConverter { @Override protected InventoryType inventoryType() { return InventoryType.BLAST_FURNACE; } }
    public static class Smoker extends AbstractFurnaceInventoryConverter { @Override protected InventoryType inventoryType() { return InventoryType.SMOKER; } }
    public static class BrewingStand extends CraftTileInventoryConverter { @Override protected InventoryType inventoryType() { return InventoryType.BREWING; } }
    public static class Dispenser extends CraftTileInventoryConverter { @Override protected InventoryType inventoryType() { return InventoryType.DISPENSER; } }
    public static class Dropper extends CraftTileInventoryConverter { @Override protected InventoryType inventoryType() { return InventoryType.DROPPER; } }
    public static class Hopper extends CraftTileInventoryConverter { @Override protected InventoryType inventoryType() { return InventoryType.HOPPER; } }
    public static class Lectern extends CraftTileInventoryConverter { @Override protected InventoryType inventoryType() { return InventoryType.LECTERN; } }
}
