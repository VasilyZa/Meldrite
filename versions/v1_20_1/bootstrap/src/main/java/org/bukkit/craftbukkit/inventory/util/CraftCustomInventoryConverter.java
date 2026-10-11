package org.bukkit.craftbukkit.inventory.util;

import org.bukkit.craftbukkit.inventory.CraftInventoryCustom;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Converter for inventories without a native tile entity. */
public class CraftCustomInventoryConverter implements CraftInventoryCreator.InventoryConverter {
    @Override public Inventory createInventory(InventoryHolder holder, InventoryType type) { return new CraftInventoryCustom(holder, type); }
    @Override public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) { return new CraftInventoryCustom(holder, type, title); }
    @Override public Inventory createInventory(InventoryHolder holder, InventoryType type, net.kyori.adventure.text.Component title) { return new CraftInventoryCustom(holder, type, title); }
    public Inventory createInventory(InventoryHolder holder, int size) { return new CraftInventoryCustom(holder, size); }
    public Inventory createInventory(InventoryHolder holder, int size, String title) { return new CraftInventoryCustom(holder, size, title); }
    public Inventory createInventory(InventoryHolder holder, int size, net.kyori.adventure.text.Component title) { return new CraftInventoryCustom(holder, size, title); }
}
