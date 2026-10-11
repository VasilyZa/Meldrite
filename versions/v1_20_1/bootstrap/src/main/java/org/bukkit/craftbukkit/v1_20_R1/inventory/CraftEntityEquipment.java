package org.bukkit.craftbukkit.v1_20_R1.inventory;

import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class CraftEntityEquipment implements org.bukkit.inventory.EntityEquipment {
    private final CraftLivingEntity owner;
    public CraftEntityEquipment(CraftLivingEntity owner) { this.owner = java.util.Objects.requireNonNull(owner); }
    public static net.minecraft.entity.EquipmentSlot nativeSlot(EquipmentSlot slot) { return switch (java.util.Objects.requireNonNull(slot)) { case HAND -> net.minecraft.entity.EquipmentSlot.MAINHAND; case OFF_HAND -> net.minecraft.entity.EquipmentSlot.OFFHAND; case FEET -> net.minecraft.entity.EquipmentSlot.FEET; case LEGS -> net.minecraft.entity.EquipmentSlot.LEGS; case CHEST -> net.minecraft.entity.EquipmentSlot.CHEST; case HEAD -> net.minecraft.entity.EquipmentSlot.HEAD; }; }
    @Override public ItemStack getItem(EquipmentSlot slot) { return CraftItemStack.asCraftMirror(owner.getHandle().getEquippedStack(nativeSlot(slot))); }
    @Override public void setItem(EquipmentSlot slot, ItemStack item) { setItem(slot, item, false); }
    @Override public void setItem(EquipmentSlot slot, ItemStack item, boolean silent) { owner.requireWritable(); var entity = owner.getHandle(); var nativeSlot = nativeSlot(slot); boolean previous = entity.isSilent(); if (silent) entity.setSilent(true); try { entity.equipStack(nativeSlot, CraftItemStack.asNMSCopy(item)); } finally { if (silent) entity.setSilent(previous); } }
    @Override public ItemStack getItemInMainHand() { return getItem(EquipmentSlot.HAND); }
    @Override public void setItemInMainHand(ItemStack item) { setItem(EquipmentSlot.HAND, item); }
    @Override public void setItemInMainHand(ItemStack item, boolean silent) { setItem(EquipmentSlot.HAND, item, silent); }
    @Override public ItemStack getItemInOffHand() { return getItem(EquipmentSlot.OFF_HAND); }
    @Override public void setItemInOffHand(ItemStack item) { setItem(EquipmentSlot.OFF_HAND, item); }
    @Override public void setItemInOffHand(ItemStack item, boolean silent) { setItem(EquipmentSlot.OFF_HAND, item, silent); }
    @Override public ItemStack getItemInHand() { return getItemInMainHand(); }
    @Override public void setItemInHand(ItemStack item) { setItemInMainHand(item); }
    @Override public ItemStack getHelmet() { return getItem(EquipmentSlot.HEAD); }
    @Override public void setHelmet(ItemStack item) { setItem(EquipmentSlot.HEAD, item); }
    @Override public void setHelmet(ItemStack item, boolean silent) { setItem(EquipmentSlot.HEAD, item, silent); }
    @Override public ItemStack getChestplate() { return getItem(EquipmentSlot.CHEST); }
    @Override public void setChestplate(ItemStack item) { setItem(EquipmentSlot.CHEST, item); }
    @Override public void setChestplate(ItemStack item, boolean silent) { setItem(EquipmentSlot.CHEST, item, silent); }
    @Override public ItemStack getLeggings() { return getItem(EquipmentSlot.LEGS); }
    @Override public void setLeggings(ItemStack item) { setItem(EquipmentSlot.LEGS, item); }
    @Override public void setLeggings(ItemStack item, boolean silent) { setItem(EquipmentSlot.LEGS, item, silent); }
    @Override public ItemStack getBoots() { return getItem(EquipmentSlot.FEET); }
    @Override public void setBoots(ItemStack item) { setItem(EquipmentSlot.FEET, item); }
    @Override public void setBoots(ItemStack item, boolean silent) { setItem(EquipmentSlot.FEET, item, silent); }
    @Override public ItemStack[] getArmorContents() { return new ItemStack[] { getBoots(), getLeggings(), getChestplate(), getHelmet() }; }
    @Override public void setArmorContents(ItemStack[] items) { java.util.Objects.requireNonNull(items); if (items.length > 4) throw new IllegalArgumentException("Armor array is longer than four slots"); var slots = new EquipmentSlot[] { EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD }; for (int i = 0; i < slots.length; i++) setItem(slots[i], i < items.length ? items[i] : null); }
    @Override public void clear() { for (var slot : EquipmentSlot.values()) setItem(slot, null); }
    @Override public org.bukkit.entity.Entity getHolder() { return owner; }
    @Override public float getDropChance(EquipmentSlot slot) { var nativeSlot = nativeSlot(slot); if (!(owner.getHandle() instanceof net.minecraft.entity.mob.MobEntity mob)) return 1; var state = (dev.meldrite.v1201.hook.MobEntityPaperAccessMixin) mob; return (nativeSlot.isArmorSlot() ? state.meldrite$getArmorDropChances() : state.meldrite$getHandDropChances())[nativeSlot.getEntitySlotId()]; }
    @Override public void setDropChance(EquipmentSlot slot, float chance) { if (!Float.isFinite(chance) || chance < 0 || chance > 1) throw new IllegalArgumentException("Drop chance outside [0,1]"); owner.requireWritable(); var nativeSlot = nativeSlot(slot); if (!(owner.getHandle() instanceof net.minecraft.entity.mob.MobEntity mob)) throw new IllegalArgumentException("Cannot set drop chance for non-Mob entity"); var state = (dev.meldrite.v1201.hook.MobEntityPaperAccessMixin) mob; (nativeSlot.isArmorSlot() ? state.meldrite$getArmorDropChances() : state.meldrite$getHandDropChances())[nativeSlot.getEntitySlotId()] = chance; }
    @Override public float getItemInHandDropChance() { return getItemInMainHandDropChance(); }
    @Override public void setItemInHandDropChance(float chance) { setItemInMainHandDropChance(chance); }
    @Override public float getItemInMainHandDropChance() { return getDropChance(EquipmentSlot.HAND); }
    @Override public void setItemInMainHandDropChance(float chance) { setDropChance(EquipmentSlot.HAND, chance); }
    @Override public float getItemInOffHandDropChance() { return getDropChance(EquipmentSlot.OFF_HAND); }
    @Override public void setItemInOffHandDropChance(float chance) { setDropChance(EquipmentSlot.OFF_HAND, chance); }
    @Override public float getHelmetDropChance() { return getDropChance(EquipmentSlot.HEAD); }
    @Override public void setHelmetDropChance(float chance) { setDropChance(EquipmentSlot.HEAD, chance); }
    @Override public float getChestplateDropChance() { return getDropChance(EquipmentSlot.CHEST); }
    @Override public void setChestplateDropChance(float chance) { setDropChance(EquipmentSlot.CHEST, chance); }
    @Override public float getLeggingsDropChance() { return getDropChance(EquipmentSlot.LEGS); }
    @Override public void setLeggingsDropChance(float chance) { setDropChance(EquipmentSlot.LEGS, chance); }
    @Override public float getBootsDropChance() { return getDropChance(EquipmentSlot.FEET); }
    @Override public void setBootsDropChance(float chance) { setDropChance(EquipmentSlot.FEET, chance); }
}
