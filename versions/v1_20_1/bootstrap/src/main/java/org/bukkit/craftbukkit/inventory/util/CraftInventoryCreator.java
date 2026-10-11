package org.bukkit.craftbukkit.inventory.util;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Central inventory factory retained for legacy CraftBukkit callers.  The
 * server factory owns the native container selection and title handling, so
 * every conversion follows the same live inventory path as Bukkit itself.
 */
public final class CraftInventoryCreator {
    public static final CraftInventoryCreator INSTANCE = new CraftInventoryCreator();
    private CraftInventoryCreator() { }

    public Inventory createInventory(InventoryHolder holder, InventoryType type) {
        return Bukkit.getServer().createInventory(holder, Objects.requireNonNull(type, "type"));
    }
    public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        return Bukkit.getServer().createInventory(holder, Objects.requireNonNull(type, "type"), title);
    }
    public Inventory createInventory(InventoryHolder holder, InventoryType type, net.kyori.adventure.text.Component title) {
        return Bukkit.getServer().createInventory(holder, Objects.requireNonNull(type, "type"), title);
    }
    public Inventory createInventory(InventoryHolder holder, int size) {
        return Bukkit.getServer().createInventory(holder, size);
    }
    public Inventory createInventory(InventoryHolder holder, int size, String title) {
        return Bukkit.getServer().createInventory(holder, size, title);
    }
    public Inventory createInventory(InventoryHolder holder, int size, net.kyori.adventure.text.Component title) {
        return Bukkit.getServer().createInventory(holder, size, title);
    }

    public interface InventoryConverter {
        Inventory createInventory(InventoryHolder holder, InventoryType type);
        Inventory createInventory(InventoryHolder holder, InventoryType type, String title);
        Inventory createInventory(InventoryHolder holder, InventoryType type, net.kyori.adventure.text.Component title);
    }
}
