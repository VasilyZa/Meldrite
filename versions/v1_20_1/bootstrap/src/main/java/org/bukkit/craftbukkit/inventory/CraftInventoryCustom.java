package org.bukkit.craftbukkit.inventory;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

/** Unversioned ABI entry point retained for plugins that bypass class remapping. */
public class CraftInventoryCustom extends org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryCustom {
    public CraftInventoryCustom(InventoryHolder owner, InventoryType type) { super(owner, type); }
    public CraftInventoryCustom(InventoryHolder owner, InventoryType type, String title) { super(owner, type, title); }
    public CraftInventoryCustom(InventoryHolder owner, InventoryType type, net.kyori.adventure.text.Component title) { super(owner, type, title); }
    public CraftInventoryCustom(InventoryHolder owner, int size) { super(owner, size); }
    public CraftInventoryCustom(InventoryHolder owner, int size, String title) { super(owner, size, title); }
    public CraftInventoryCustom(InventoryHolder owner, int size, net.kyori.adventure.text.Component title) { super(owner, size, title); }
}
