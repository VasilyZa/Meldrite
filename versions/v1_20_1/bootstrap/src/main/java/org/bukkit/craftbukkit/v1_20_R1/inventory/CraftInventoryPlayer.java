package org.bukkit.craftbukkit.v1_20_R1.inventory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;

/** Live Bukkit view of the native 1.20.1 player inventory. */
public final class CraftInventoryPlayer extends CraftInventory implements org.bukkit.inventory.PlayerInventory, EntityEquipment {
    private volatile PlayerInventory playerInventory;

    @Override public void meldrite$rebindInventory(net.minecraft.inventory.Inventory replacement) {
        if (!(replacement instanceof PlayerInventory next)) throw new IllegalArgumentException("PLAYER_INVENTORY_REBIND_REQUIRES_PLAYER_INVENTORY");
        super.meldrite$rebindInventory(replacement);
        playerInventory = next;
    }

    public CraftInventoryPlayer(PlayerInventory inventory) {
        super(inventory, null, InventoryType.PLAYER);
        this.playerInventory = inventory;
    }

    public PlayerInventory getInventory() { return playerInventory; }

    @Override public void setItem(int index, org.bukkit.inventory.ItemStack item) {
        checkIndex(index);
        if (playerInventory.player == null) {
            // A null-player PlayerInventory is used by native startup probes
            // and by a few libraries while they build detached containers.
            // PlayerInventory#setStack is intentionally allowed to assume a
            // live PlayerScreenHandler, so write directly to its slot list in
            // this detached case.
            net.minecraft.item.ItemStack nativeStack = nativeStack(item);
            if (index < playerInventory.main.size()) playerInventory.main.set(index, nativeStack);
            else if (index < playerInventory.main.size() + playerInventory.armor.size())
                playerInventory.armor.set(index - playerInventory.main.size(), nativeStack);
            else playerInventory.offHand.set(index - playerInventory.main.size() - playerInventory.armor.size(), nativeStack);
            return;
        }
        super.setItem(index, item);
    }

    @Override public int getSize() { return playerInventory.size(); }

    @Override public org.bukkit.inventory.ItemStack[] getStorageContents() {
        org.bukkit.inventory.ItemStack[] result = new org.bukkit.inventory.ItemStack[playerInventory.main.size()];
        for (int index = 0; index < result.length; index++) result[index] = craftStack(playerInventory.main.get(index));
        return result;
    }

    @Override public void setStorageContents(org.bukkit.inventory.ItemStack[] contents) {
        setSlots(contents, 0, playerInventory.main.size());
    }

    @Override public org.bukkit.inventory.ItemStack[] getArmorContents() {
        org.bukkit.inventory.ItemStack[] result = new org.bukkit.inventory.ItemStack[playerInventory.armor.size()];
        for (int index = 0; index < result.length; index++) result[index] = craftStack(playerInventory.armor.get(index));
        return result;
    }

    @Override public void setArmorContents(org.bukkit.inventory.ItemStack[] contents) {
        setSlots(contents, playerInventory.main.size(), playerInventory.armor.size());
    }

    @Override public org.bukkit.inventory.ItemStack[] getExtraContents() {
        org.bukkit.inventory.ItemStack[] result = new org.bukkit.inventory.ItemStack[playerInventory.offHand.size()];
        for (int index = 0; index < result.length; index++) result[index] = craftStack(playerInventory.offHand.get(index));
        return result;
    }

    @Override public void setExtraContents(org.bukkit.inventory.ItemStack[] contents) {
        setSlots(contents, playerInventory.main.size() + playerInventory.armor.size(), playerInventory.offHand.size());
    }

    private void setSlots(org.bukkit.inventory.ItemStack[] contents, int base, int length) {
        if (contents == null) contents = new org.bukkit.inventory.ItemStack[0];
        if (contents.length > length) throw new IllegalArgumentException("contents is larger than the native slot group");
        for (int index = 0; index < length; index++) setItem(base + index, index < contents.length ? contents[index] : null);
    }

    @Override public org.bukkit.inventory.ItemStack getItemInMainHand() { return craftStack(playerInventory.getMainHandStack()); }
    @Override public void setItemInMainHand(org.bukkit.inventory.ItemStack item) { setItem(getHeldItemSlot(), item); }
    @Override public void setItemInMainHand(org.bukkit.inventory.ItemStack item, boolean silent) { setItemInMainHand(item); }
    @Override public org.bukkit.inventory.ItemStack getItemInOffHand() { return craftStack(playerInventory.offHand.get(0)); }
    @Override public void setItemInOffHand(org.bukkit.inventory.ItemStack item) { setExtraContents(new org.bukkit.inventory.ItemStack[] { item }); }
    @Override public void setItemInOffHand(org.bukkit.inventory.ItemStack item, boolean silent) { setItemInOffHand(item); }
    @Override public org.bukkit.inventory.ItemStack getItemInHand() { return getItemInMainHand(); }
    @Override public void setItemInHand(org.bukkit.inventory.ItemStack item) { setItemInMainHand(item); }

    @Override public int getHeldItemSlot() { return playerInventory.selectedSlot; }
    @Override public void setHeldItemSlot(int slot) {
        if (slot < 0 || slot >= PlayerInventory.getHotbarSize()) throw new IllegalArgumentException("Slot must be between 0 and 8");
        playerInventory.selectedSlot = slot;
        playerInventory.markDirty();
    }

    @Override public org.bukkit.inventory.ItemStack getItem(EquipmentSlot slot) {
        Objects.requireNonNull(slot, "slot");
        return switch (slot) {
            case HAND -> getItemInMainHand();
            case OFF_HAND -> getItemInOffHand();
            case FEET -> nonNull(getBoots());
            case LEGS -> nonNull(getLeggings());
            case CHEST -> nonNull(getChestplate());
            case HEAD -> nonNull(getHelmet());
        };
    }

    @Override public void setItem(EquipmentSlot slot, org.bukkit.inventory.ItemStack item) { setItem(slot, item, false); }
    @Override public void setItem(EquipmentSlot slot, org.bukkit.inventory.ItemStack item, boolean silent) {
        Objects.requireNonNull(slot, "slot");
        switch (slot) {
            case HAND -> setItemInMainHand(item);
            case OFF_HAND -> setItemInOffHand(item);
            case FEET -> setBoots(item);
            case LEGS -> setLeggings(item);
            case CHEST -> setChestplate(item);
            case HEAD -> setHelmet(item);
        }
    }

    private static org.bukkit.inventory.ItemStack nonNull(org.bukkit.inventory.ItemStack item) {
        return item == null ? new org.bukkit.inventory.ItemStack(Material.AIR) : item;
    }

    @Override public org.bukkit.inventory.ItemStack getHelmet() { return craftStack(playerInventory.getArmorStack(3)); }
    @Override public void setHelmet(org.bukkit.inventory.ItemStack item) { setItem(39, item); }
    @Override public void setHelmet(org.bukkit.inventory.ItemStack item, boolean silent) { setHelmet(item); }
    @Override public org.bukkit.inventory.ItemStack getChestplate() { return craftStack(playerInventory.getArmorStack(2)); }
    @Override public void setChestplate(org.bukkit.inventory.ItemStack item) { setItem(38, item); }
    @Override public void setChestplate(org.bukkit.inventory.ItemStack item, boolean silent) { setChestplate(item); }
    @Override public org.bukkit.inventory.ItemStack getLeggings() { return craftStack(playerInventory.getArmorStack(1)); }
    @Override public void setLeggings(org.bukkit.inventory.ItemStack item) { setItem(37, item); }
    @Override public void setLeggings(org.bukkit.inventory.ItemStack item, boolean silent) { setLeggings(item); }
    @Override public org.bukkit.inventory.ItemStack getBoots() { return craftStack(playerInventory.getArmorStack(0)); }
    @Override public void setBoots(org.bukkit.inventory.ItemStack item) { setItem(36, item); }
    @Override public void setBoots(org.bukkit.inventory.ItemStack item, boolean silent) { setBoots(item); }

    @Override public HumanEntity getHolder() {
        PlayerEntity player = playerInventory.player;
        if (player == null) return null;
        try {
            return (HumanEntity) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(player);
        } catch (IllegalStateException unavailable) {
            return null;
        }
    }

    @Override public List<HumanEntity> getViewers() {
        LinkedHashSet<HumanEntity> result = new LinkedHashSet<>(super.getViewers());
        HumanEntity holder = getHolder();
        if (holder != null) result.add(holder);
        if (playerInventory instanceof dev.meldrite.v1201.bridge.PlayerInventoryViewersAccess access) {
            for (PlayerEntity player : access.meldrite$getViewers()) {
                try { result.add((HumanEntity) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(player)); }
                catch (IllegalStateException ignored) { }
            }
        }
        return List.copyOf(result);
    }

    @Override public float getItemInHandDropChance() { return 1.0f; }
    @Override public void setItemInHandDropChance(float chance) { throw unsupportedDropChance(); }
    @Override public float getItemInMainHandDropChance() { return 1.0f; }
    @Override public void setItemInMainHandDropChance(float chance) { throw unsupportedDropChance(); }
    @Override public float getItemInOffHandDropChance() { return 1.0f; }
    @Override public void setItemInOffHandDropChance(float chance) { throw unsupportedDropChance(); }
    @Override public float getHelmetDropChance() { return 1.0f; }
    @Override public void setHelmetDropChance(float chance) { throw unsupportedDropChance(); }
    @Override public float getChestplateDropChance() { return 1.0f; }
    @Override public void setChestplateDropChance(float chance) { throw unsupportedDropChance(); }
    @Override public float getLeggingsDropChance() { return 1.0f; }
    @Override public void setLeggingsDropChance(float chance) { throw unsupportedDropChance(); }
    @Override public float getBootsDropChance() { return 1.0f; }
    @Override public void setBootsDropChance(float chance) { throw unsupportedDropChance(); }
    @Override public float getDropChance(EquipmentSlot slot) { Objects.requireNonNull(slot, "slot"); return 1.0f; }
    @Override public void setDropChance(EquipmentSlot slot, float chance) { throw unsupportedDropChance(); }

    private static UnsupportedOperationException unsupportedDropChance() {
        return new UnsupportedOperationException("Cannot set drop chance for PlayerInventory");
    }
}
