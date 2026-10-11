package org.bukkit.craftbukkit.v1_20_R1.inventory;

import java.util.Objects;
import net.minecraft.inventory.Inventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/** Input slots followed by the native result slots, as exposed by Bukkit. */
public class CraftResultInventory extends CraftInventory {
    private final Inventory resultInventory;
    public CraftResultInventory(Inventory inventory, Inventory resultInventory) {
        super(inventory);
        this.resultInventory = Objects.requireNonNull(resultInventory, "resultInventory");
    }
    protected CraftResultInventory(Inventory inventory, Inventory resultInventory, InventoryType type) {
        super(inventory, null, type);
        this.resultInventory = Objects.requireNonNull(resultInventory, "resultInventory");
    }
    public Inventory getIngredientsInventory() { return inventory; }
    public Inventory getResultInventory() { return resultInventory; }
    @Override public int getSize() { return inventory.size() + resultInventory.size(); }
    @Override public ItemStack getItem(int slot) {
        checkIndex(slot);
        return craftStack(slot < inventory.size() ? inventory.getStack(slot) : resultInventory.getStack(slot - inventory.size()));
    }
    @Override public void setItem(int slot, ItemStack item) {
        checkIndex(slot);
        Inventory target = slot < inventory.size() ? inventory : resultInventory;
        target.setStack(slot < inventory.size() ? slot : slot - inventory.size(), nativeStack(item));
    }
    @Override public ItemStack[] getContents() {
        ItemStack[] contents = new ItemStack[getSize()];
        for (int i = 0; i < contents.length; i++) contents[i] = getItem(i);
        return contents;
    }
    @Override public void clear() { inventory.clear(); inventory.markDirty(); resultInventory.clear(); }
}
