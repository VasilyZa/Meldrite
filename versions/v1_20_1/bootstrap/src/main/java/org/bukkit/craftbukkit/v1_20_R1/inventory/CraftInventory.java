package org.bukkit.craftbukkit.v1_20_R1.inventory;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;

/**
 * CraftBukkit-style live wrapper for a 1.20.1 native inventory.
 *
 * The wrapper deliberately keeps native stack identity for reads.  A plugin
 * changing the returned CraftItemStack therefore changes the server stack in
 * the same way as CraftBukkit's mirror implementation.
 */
public class CraftInventory implements org.bukkit.inventory.Inventory {
    private static final Map<Inventory, Set<HumanEntity>> VIEWERS =
            Collections.synchronizedMap(new IdentityHashMap<>());

    protected volatile Inventory inventory;
    private final org.bukkit.inventory.InventoryHolder holder;
    private final InventoryType type;
    private final String title;
    private int maxStackSize;

    /** Native player replacement keeps externally held Bukkit inventory views live. */
    public void meldrite$rebindInventory(Inventory replacement) {
        Objects.requireNonNull(replacement, "replacement");
        synchronized (VIEWERS) {
            var viewers = VIEWERS.remove(inventory);
            if (viewers != null) VIEWERS.computeIfAbsent(replacement, ignored -> new LinkedHashSet<>()).addAll(viewers);
            inventory = replacement;
        }
    }

    public CraftInventory(Inventory inventory) {
        this(inventory, null, detectType(inventory));
    }

    public CraftInventory(Inventory inventory, org.bukkit.inventory.InventoryHolder holder) {
        this(inventory, holder, detectType(inventory));
    }

    public CraftInventory(Inventory inventory, org.bukkit.inventory.InventoryHolder holder, InventoryType type) {
        this(inventory, holder, type, type.getDefaultTitle());
    }

    public CraftInventory(Inventory inventory, org.bukkit.inventory.InventoryHolder holder, InventoryType type, String title) {
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.holder = holder;
        this.type = Objects.requireNonNull(type, "type");
        this.title = title == null ? type.getDefaultTitle() : title;
        this.maxStackSize = inventory.getMaxCountPerStack();
    }

    public Inventory getInventory() { return inventory; }

    public static void registerViewer(Inventory inventory, HumanEntity viewer) {
        if (inventory == null || viewer == null) return;
        synchronized (VIEWERS) {
            VIEWERS.computeIfAbsent(inventory, ignored -> new LinkedHashSet<>()).add(viewer);
        }
    }

    public static void unregisterViewer(Inventory inventory, HumanEntity viewer) {
        if (inventory == null || viewer == null) return;
        synchronized (VIEWERS) {
            Set<HumanEntity> viewers = VIEWERS.get(inventory);
            if (viewers == null) return;
            viewers.remove(viewer);
            if (viewers.isEmpty()) VIEWERS.remove(inventory);
        }
    }

    protected static ItemStack nativeStack(org.bukkit.inventory.ItemStack stack) {
        return CraftItemStack.asNMSCopy(stack);
    }

    protected static org.bukkit.inventory.ItemStack craftStack(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : CraftItemStack.asCraftMirror(stack);
    }

    protected org.bukkit.inventory.ItemStack[] mirrorContents(int size) {
        org.bukkit.inventory.ItemStack[] result = new org.bukkit.inventory.ItemStack[size];
        for (int index = 0; index < size; index++) result[index] = craftStack(inventory.getStack(index));
        return result;
    }

    @Override public int getSize() { return inventory.size(); }
    @Override public int getMaxStackSize() { return maxStackSize; }
    @Override public void setMaxStackSize(int size) {
        if (size < 1) throw new IllegalArgumentException("size must be positive");
        maxStackSize = size;
    }

    @Override public org.bukkit.inventory.ItemStack getItem(int index) {
        checkIndex(index);
        return craftStack(inventory.getStack(index));
    }

    @Override public void setItem(int index, org.bukkit.inventory.ItemStack stack) {
        checkIndex(index);
        inventory.setStack(index, nativeStack(stack));
        inventory.markDirty();
    }

    @Override public org.bukkit.inventory.ItemStack[] getContents() { return mirrorContents(getSize()); }
    @Override public org.bukkit.inventory.ItemStack[] getStorageContents() { return getContents(); }

    @Override public void setContents(org.bukkit.inventory.ItemStack[] contents) {
        Objects.requireNonNull(contents, "contents");
        if (contents.length > getSize()) throw new IllegalArgumentException("Invalid inventory size");
        for (int index = 0; index < getSize(); index++) setItem(index, index < contents.length ? contents[index] : null);
    }

    @Override public void setStorageContents(org.bukkit.inventory.ItemStack[] contents) { setContents(contents); }

    @Override public HashMap<Integer, org.bukkit.inventory.ItemStack> addItem(org.bukkit.inventory.ItemStack... stacks) {
        Objects.requireNonNull(stacks, "stacks");
        HashMap<Integer, org.bukkit.inventory.ItemStack> leftovers = new HashMap<>();
        for (int sourceIndex = 0; sourceIndex < stacks.length; sourceIndex++) {
            org.bukkit.inventory.ItemStack source = Objects.requireNonNull(stacks[sourceIndex], "ItemStack");
            org.bukkit.inventory.ItemStack remaining = source.clone();
            while (remaining.getAmount() > 0) {
                int partial = firstPartial(remaining);
                if (partial < 0) {
                    int empty = firstEmpty();
                    if (empty < 0) break;
                    int amount = Math.min(remaining.getAmount(), Math.min(getMaxStackSize(), remaining.getMaxStackSize()));
                    org.bukkit.inventory.ItemStack placed = remaining.clone();
                    placed.setAmount(amount);
                    setItem(empty, placed);
                    remaining.setAmount(remaining.getAmount() - amount);
                } else {
                    org.bukkit.inventory.ItemStack existing = getItem(partial);
                    int capacity = Math.min(getMaxStackSize(), existing.getMaxStackSize()) - existing.getAmount();
                    if (capacity <= 0) {
                        // The partial scan only returns stacks with room. This
                        // guard prevents an infinite loop for unusual items.
                        break;
                    }
                    int amount = Math.min(capacity, remaining.getAmount());
                    existing.setAmount(existing.getAmount() + amount);
                    setItem(partial, existing);
                    remaining.setAmount(remaining.getAmount() - amount);
                }
            }
            if (remaining.getAmount() > 0) leftovers.put(sourceIndex, remaining);
        }
        return leftovers;
    }

    @Override public HashMap<Integer, org.bukkit.inventory.ItemStack> removeItem(org.bukkit.inventory.ItemStack... stacks) {
        return removeItems(stacks);
    }

    @Override public HashMap<Integer, org.bukkit.inventory.ItemStack> removeItemAnySlot(org.bukkit.inventory.ItemStack... stacks) {
        return removeItems(stacks);
    }

    private HashMap<Integer, org.bukkit.inventory.ItemStack> removeItems(org.bukkit.inventory.ItemStack[] stacks) {
        Objects.requireNonNull(stacks, "stacks");
        HashMap<Integer, org.bukkit.inventory.ItemStack> leftovers = new HashMap<>();
        for (int sourceIndex = 0; sourceIndex < stacks.length; sourceIndex++) {
            org.bukkit.inventory.ItemStack source = Objects.requireNonNull(stacks[sourceIndex], "ItemStack");
            org.bukkit.inventory.ItemStack remaining = source.clone();
            for (int index = 0; index < getSize() && remaining.getAmount() > 0; index++) {
                org.bukkit.inventory.ItemStack existing = getItem(index);
                if (existing == null || !source.isSimilar(existing)) continue;
                int removed = Math.min(existing.getAmount(), remaining.getAmount());
                existing.setAmount(existing.getAmount() - removed);
                remaining.setAmount(remaining.getAmount() - removed);
                setItem(index, existing.getAmount() == 0 ? null : existing);
            }
            if (remaining.getAmount() > 0) leftovers.put(sourceIndex, remaining);
        }
        return leftovers;
    }

    @Override public boolean contains(Material material) {
        Objects.requireNonNull(material, "material");
        return first(material) >= 0;
    }

    @Override public boolean contains(org.bukkit.inventory.ItemStack stack) {
        return stack != null && first(stack) >= 0;
    }

    @Override public boolean contains(Material material, int amount) {
        Objects.requireNonNull(material, "material");
        if (amount <= 0) return true;
        int found = 0;
        for (org.bukkit.inventory.ItemStack stack : getStorageContents()) {
            if (stack != null && stack.getType() == material && (found += stack.getAmount()) >= amount) return true;
        }
        return false;
    }

    @Override public boolean contains(org.bukkit.inventory.ItemStack stack, int amount) {
        if (stack == null || amount <= 0) return amount <= 0;
        int found = 0;
        for (org.bukkit.inventory.ItemStack current : getStorageContents()) {
            if (current != null && stack.isSimilar(current) && (found += current.getAmount()) >= amount) return true;
        }
        return false;
    }

    @Override public boolean containsAtLeast(org.bukkit.inventory.ItemStack stack, int amount) {
        return contains(stack, amount);
    }

    @Override public HashMap<Integer, ? extends org.bukkit.inventory.ItemStack> all(Material material) {
        Objects.requireNonNull(material, "material");
        HashMap<Integer, org.bukkit.inventory.ItemStack> result = new HashMap<>();
        org.bukkit.inventory.ItemStack[] contents = getStorageContents();
        for (int index = 0; index < contents.length; index++) {
            if (contents[index] != null && contents[index].getType() == material) result.put(index, contents[index]);
        }
        return result;
    }

    @Override public HashMap<Integer, ? extends org.bukkit.inventory.ItemStack> all(org.bukkit.inventory.ItemStack stack) {
        HashMap<Integer, org.bukkit.inventory.ItemStack> result = new HashMap<>();
        if (stack == null) return result;
        org.bukkit.inventory.ItemStack[] contents = getStorageContents();
        for (int index = 0; index < contents.length; index++) {
            if (contents[index] != null && stack.isSimilar(contents[index])) result.put(index, contents[index]);
        }
        return result;
    }

    @Override public int first(Material material) {
        Objects.requireNonNull(material, "material");
        org.bukkit.inventory.ItemStack[] contents = getStorageContents();
        for (int index = 0; index < contents.length; index++) {
            if (contents[index] != null && contents[index].getType() == material) return index;
        }
        return -1;
    }

    @Override public int first(org.bukkit.inventory.ItemStack stack) {
        if (stack == null) return -1;
        org.bukkit.inventory.ItemStack[] contents = getStorageContents();
        for (int index = 0; index < contents.length; index++) {
            if (contents[index] != null && stack.isSimilar(contents[index])) return index;
        }
        return -1;
    }

    @Override public int firstEmpty() {
        org.bukkit.inventory.ItemStack[] contents = getStorageContents();
        for (int index = 0; index < contents.length; index++) if (contents[index] == null) return index;
        return -1;
    }

    protected int firstPartial(org.bukkit.inventory.ItemStack stack) {
        if (stack == null) return -1;
        org.bukkit.inventory.ItemStack[] contents = getStorageContents();
        for (int index = 0; index < contents.length; index++) {
            org.bukkit.inventory.ItemStack current = contents[index];
            if (current != null && current.isSimilar(stack)
                    && current.getAmount() < Math.min(getMaxStackSize(), current.getMaxStackSize())) return index;
        }
        return -1;
    }

    @Override public boolean isEmpty() { return inventory.isEmpty(); }

    @Override public void remove(Material material) {
        Objects.requireNonNull(material, "material");
        for (int index = 0; index < getSize(); index++) {
            org.bukkit.inventory.ItemStack stack = getItem(index);
            if (stack != null && stack.getType() == material) setItem(index, null);
        }
    }

    @Override public void remove(org.bukkit.inventory.ItemStack stack) {
        if (stack == null) return;
        for (int index = 0; index < getSize(); index++) {
            org.bukkit.inventory.ItemStack current = getItem(index);
            if (current != null && stack.isSimilar(current)) setItem(index, null);
        }
    }

    @Override public void clear(int index) { setItem(index, null); }
    @Override public void clear() { inventory.clear(); inventory.markDirty(); }

    @Override public int close() {
        List<HumanEntity> viewers = getViewers();
        for (HumanEntity viewer : viewers) viewer.closeInventory();
        return viewers.size();
    }

    @Override public List<HumanEntity> getViewers() {
        synchronized (VIEWERS) {
            Set<HumanEntity> viewers = VIEWERS.get(inventory);
            return viewers == null ? List.of() : List.copyOf(viewers);
        }
    }

    @Override public InventoryType getType() { return type; }
    public String getTitle() { return title; }
    @Override public org.bukkit.inventory.InventoryHolder getHolder() { return holder; }
    @Override public org.bukkit.inventory.InventoryHolder getHolder(boolean useSnapshot) { return holder; }

    @Override public ListIterator<org.bukkit.inventory.ItemStack> iterator() { return iterator(0); }
    @Override public ListIterator<org.bukkit.inventory.ItemStack> iterator(int index) {
        if (index < 0 || index > getSize()) throw new IndexOutOfBoundsException("index: " + index);
        return new AbstractList<org.bukkit.inventory.ItemStack>() {
            @Override public org.bukkit.inventory.ItemStack get(int slot) { return CraftInventory.this.getItem(slot); }
            @Override public int size() { return CraftInventory.this.getSize(); }
            @Override public org.bukkit.inventory.ItemStack set(int slot, org.bukkit.inventory.ItemStack item) {
                org.bukkit.inventory.ItemStack previous = get(slot);
                CraftInventory.this.setItem(slot, item);
                return previous;
            }
        }.listIterator(index);
    }

    @Override public Location getLocation() {
        if (holder instanceof HumanEntity human) return human.getLocation();
        if (holder instanceof org.bukkit.block.BlockState blockState) return blockState.getLocation();
        return null;
    }

    protected static InventoryType detectType(Inventory inventory) {
        if (inventory instanceof net.minecraft.entity.player.PlayerInventory) return InventoryType.PLAYER;
        if (inventory instanceof EnderChestInventory) return InventoryType.ENDER_CHEST;
        if (inventory instanceof CraftingInventory) return inventory.size() >= 9 ? InventoryType.WORKBENCH : InventoryType.CRAFTING;
        if (inventory instanceof DropperBlockEntity) return InventoryType.DROPPER;
        if (inventory instanceof DispenserBlockEntity) return InventoryType.DISPENSER;
        if (inventory instanceof BlastFurnaceBlockEntity) return InventoryType.BLAST_FURNACE;
        if (inventory instanceof SmokerBlockEntity) return InventoryType.SMOKER;
        if (inventory instanceof AbstractFurnaceBlockEntity) return InventoryType.FURNACE;
        if (inventory instanceof BrewingStandBlockEntity) return InventoryType.BREWING;
        if (inventory instanceof HopperBlockEntity) return InventoryType.HOPPER;
        if (inventory instanceof ShulkerBoxBlockEntity) return InventoryType.SHULKER_BOX;
        if (inventory instanceof BarrelBlockEntity) return InventoryType.BARREL;
        return InventoryType.CHEST;
    }

    protected void checkIndex(int index) {
        if (index < 0 || index >= getSize()) throw new ArrayIndexOutOfBoundsException(index);
    }
}
