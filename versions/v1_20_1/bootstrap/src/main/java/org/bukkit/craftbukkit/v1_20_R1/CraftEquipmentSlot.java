package org.bukkit.craftbukkit.v1_20_R1;

public final class CraftEquipmentSlot {
    private CraftEquipmentSlot() { }
    public static org.bukkit.inventory.EquipmentSlot getSlot(net.minecraft.entity.EquipmentSlot slot) { return switch (slot) { case MAINHAND -> org.bukkit.inventory.EquipmentSlot.HAND; case OFFHAND -> org.bukkit.inventory.EquipmentSlot.OFF_HAND; default -> org.bukkit.inventory.EquipmentSlot.valueOf(slot.name()); }; }
    public static net.minecraft.entity.EquipmentSlot getNMS(org.bukkit.inventory.EquipmentSlot slot) { return switch (slot) { case HAND -> net.minecraft.entity.EquipmentSlot.MAINHAND; case OFF_HAND -> net.minecraft.entity.EquipmentSlot.OFFHAND; default -> net.minecraft.entity.EquipmentSlot.valueOf(slot.name()); }; }
    public static org.bukkit.inventory.EquipmentSlot getHand(net.minecraft.util.Hand hand) { return hand == net.minecraft.util.Hand.MAIN_HAND ? org.bukkit.inventory.EquipmentSlot.HAND : org.bukkit.inventory.EquipmentSlot.OFF_HAND; }
    public static net.minecraft.util.Hand getHand(org.bukkit.inventory.EquipmentSlot hand) { return switch (hand) { case HAND -> net.minecraft.util.Hand.MAIN_HAND; case OFF_HAND -> net.minecraft.util.Hand.OFF_HAND; default -> throw new IllegalArgumentException("Equipment slot is not a hand: " + hand); }; }
}
