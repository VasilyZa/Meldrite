package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.inventory.SimpleInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.inventory.InventoryType;

/** Live CraftBukkit wrapper for plugin-created inventories. */
public class CraftInventoryCustom extends CraftInventory {
    public CraftInventoryCustom(InventoryHolder owner, InventoryType type) {
        this(owner, type, type == null ? null : type.getDefaultTitle());
    }

    public CraftInventoryCustom(InventoryHolder owner, InventoryType type, String title) {
        this(owner, type == null ? 0 : type.getDefaultSize(), type, title);
    }

    public CraftInventoryCustom(InventoryHolder owner, InventoryType type, net.kyori.adventure.text.Component title) {
        this(owner, type, title == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(title));
    }

    public CraftInventoryCustom(InventoryHolder owner, int size) {
        this(owner, size, "Chest");
    }

    public CraftInventoryCustom(InventoryHolder owner, int size, String title) {
        this(owner, size, InventoryType.CHEST, title);
    }

    public CraftInventoryCustom(InventoryHolder owner, int size, net.kyori.adventure.text.Component title) {
        this(owner, size, title == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(title));
    }

    private CraftInventoryCustom(InventoryHolder owner, int size, InventoryType type, String title) {
        super(new MinecraftInventory(Math.max(0, size)), owner, type == null ? InventoryType.CHEST : type,
                title == null ? (type == null ? InventoryType.CHEST.getDefaultTitle() : type.getDefaultTitle()) : title);
    }

    /** Native container retained for plugins that inspect the Craft wrapper. */
    public static class MinecraftInventory extends SimpleInventory {
        public MinecraftInventory(int size) { super(size); }
    }

    public String getTitle() { return super.getTitle(); }
    public net.kyori.adventure.text.Component title() {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(getTitle());
    }
}
