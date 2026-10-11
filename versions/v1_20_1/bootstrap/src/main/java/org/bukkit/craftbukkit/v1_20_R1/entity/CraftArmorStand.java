package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.ArmorStandNativeAccess;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftEntityEquipment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import io.papermc.paper.math.Rotations;

public class CraftArmorStand extends org.bukkit.craftbukkit.entity.CraftArmorStand implements ArmorStand {
    public CraftArmorStand(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ArmorStandEntity getHandle() { return (ArmorStandEntity) super.getHandle(); }
    @Override public net.kyori.adventure.text.Component customName() { return super.customName(); }
    @Override public void customName(net.kyori.adventure.text.Component name) { super.customName(name); }
    private ArmorStandNativeAccess state() { return (ArmorStandNativeAccess) getHandle(); }
    private static EulerAngle bukkit(net.minecraft.util.math.EulerAngle angle) { return new EulerAngle(Math.toRadians(angle.getPitch()), Math.toRadians(angle.getYaw()), Math.toRadians(angle.getRoll())); }
    private static net.minecraft.util.math.EulerAngle nativeAngle(EulerAngle angle) { java.util.Objects.requireNonNull(angle); return degrees(Math.toDegrees(angle.getX()), Math.toDegrees(angle.getY()), Math.toDegrees(angle.getZ())); }
    private static net.minecraft.util.math.EulerAngle nativeAngle(Rotations rotations) { java.util.Objects.requireNonNull(rotations); return degrees(rotations.x(), rotations.y(), rotations.z()); }
    private static net.minecraft.util.math.EulerAngle degrees(double x, double y, double z) { if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) throw new IllegalArgumentException("Nonfinite pose"); return new net.minecraft.util.math.EulerAngle((float) x, (float) y, (float) z); }
    private static Rotations rotations(net.minecraft.util.math.EulerAngle angle) { return Rotations.ofDegrees(angle.getPitch(), angle.getYaw(), angle.getRoll()); }
    @Override public ItemStack getItemInHand() { return getItem(EquipmentSlot.HAND); }
    @Override public void setItemInHand(ItemStack item) { setItem(EquipmentSlot.HAND, item); }
    @Override public ItemStack getBoots() { return getItem(EquipmentSlot.FEET); }
    @Override public void setBoots(ItemStack item) { setItem(EquipmentSlot.FEET, item); }
    @Override public ItemStack getLeggings() { return getItem(EquipmentSlot.LEGS); }
    @Override public void setLeggings(ItemStack item) { setItem(EquipmentSlot.LEGS, item); }
    @Override public ItemStack getChestplate() { return getItem(EquipmentSlot.CHEST); }
    @Override public void setChestplate(ItemStack item) { setItem(EquipmentSlot.CHEST, item); }
    @Override public ItemStack getHelmet() { return getItem(EquipmentSlot.HEAD); }
    @Override public void setHelmet(ItemStack item) { setItem(EquipmentSlot.HEAD, item); }
    @Override public ItemStack getItem(EquipmentSlot slot) { return getEquipment().getItem(slot); }
    @Override public void setItem(EquipmentSlot slot, ItemStack item) { getEquipment().setItem(slot, item); }
    @Override public EulerAngle getHeadPose() { return bukkit(getHandle().getHeadRotation()); }
    @Override public void setHeadPose(EulerAngle pose) { requireWritable(); getHandle().setHeadRotation(nativeAngle(pose)); }
    @Override public EulerAngle getBodyPose() { return bukkit(getHandle().getBodyRotation()); }
    @Override public void setBodyPose(EulerAngle pose) { requireWritable(); getHandle().setBodyRotation(nativeAngle(pose)); }
    @Override public EulerAngle getLeftArmPose() { return bukkit(getHandle().getLeftArmRotation()); }
    @Override public void setLeftArmPose(EulerAngle pose) { requireWritable(); getHandle().setLeftArmRotation(nativeAngle(pose)); }
    @Override public EulerAngle getRightArmPose() { return bukkit(getHandle().getRightArmRotation()); }
    @Override public void setRightArmPose(EulerAngle pose) { requireWritable(); getHandle().setRightArmRotation(nativeAngle(pose)); }
    @Override public EulerAngle getLeftLegPose() { return bukkit(getHandle().getLeftLegRotation()); }
    @Override public void setLeftLegPose(EulerAngle pose) { requireWritable(); getHandle().setLeftLegRotation(nativeAngle(pose)); }
    @Override public EulerAngle getRightLegPose() { return bukkit(getHandle().getRightLegRotation()); }
    @Override public void setRightLegPose(EulerAngle pose) { requireWritable(); getHandle().setRightLegRotation(nativeAngle(pose)); }
    @Override public Rotations getHeadRotations() { return rotations(getHandle().getHeadRotation()); }
    @Override public void setHeadRotations(Rotations pose) { requireWritable(); getHandle().setHeadRotation(nativeAngle(pose)); }
    @Override public Rotations getBodyRotations() { return rotations(getHandle().getBodyRotation()); }
    @Override public void setBodyRotations(Rotations pose) { requireWritable(); getHandle().setBodyRotation(nativeAngle(pose)); }
    @Override public Rotations getLeftArmRotations() { return rotations(getHandle().getLeftArmRotation()); }
    @Override public void setLeftArmRotations(Rotations pose) { requireWritable(); getHandle().setLeftArmRotation(nativeAngle(pose)); }
    @Override public Rotations getRightArmRotations() { return rotations(getHandle().getRightArmRotation()); }
    @Override public void setRightArmRotations(Rotations pose) { requireWritable(); getHandle().setRightArmRotation(nativeAngle(pose)); }
    @Override public Rotations getLeftLegRotations() { return rotations(getHandle().getLeftLegRotation()); }
    @Override public void setLeftLegRotations(Rotations pose) { requireWritable(); getHandle().setLeftLegRotation(nativeAngle(pose)); }
    @Override public Rotations getRightLegRotations() { return rotations(getHandle().getRightLegRotation()); }
    @Override public void setRightLegRotations(Rotations pose) { requireWritable(); getHandle().setRightLegRotation(nativeAngle(pose)); }
    @Override public boolean hasBasePlate() { return !getHandle().shouldHideBasePlate(); }
    @Override public void setBasePlate(boolean basePlate) { requireWritable(); getHandle().setHideBasePlate(!basePlate); }
    @Override public boolean isVisible() { return !getHandle().isInvisible(); }
    @Override public void setVisible(boolean visible) { setInvisible(!visible); }
    @Override public boolean hasArms() { return getHandle().shouldShowArms(); }
    @Override public void setArms(boolean arms) { requireWritable(); getHandle().setShowArms(arms); }
    @Override public boolean isSmall() { return getHandle().isSmall(); }
    @Override public void setSmall(boolean small) { requireWritable(); state().meldrite$setSmall(small); }
    @Override public boolean isMarker() { return getHandle().isMarker(); }
    @Override public void setMarker(boolean marker) { requireWritable(); state().meldrite$setMarker(marker); }
    @Override public void setGravity(boolean gravity) { super.setGravity(gravity); getHandle().noClip = !gravity; }
    private static int lock(EquipmentSlot slot, LockType lock) { return 1 << (CraftEntityEquipment.nativeSlot(slot).getArmorStandSlotId() + java.util.Objects.requireNonNull(lock).ordinal() * 8); }
    @Override public void addEquipmentLock(EquipmentSlot slot, LockType lock) { requireWritable(); state().meldrite$setDisabledSlots(state().meldrite$getDisabledSlots() | lock(slot, lock)); }
    @Override public void removeEquipmentLock(EquipmentSlot slot, LockType lock) { requireWritable(); state().meldrite$setDisabledSlots(state().meldrite$getDisabledSlots() & ~lock(slot, lock)); }
    @Override public boolean hasEquipmentLock(EquipmentSlot slot, LockType lock) { return (state().meldrite$getDisabledSlots() & lock(slot, lock)) != 0; }
    @Override public java.util.Set<EquipmentSlot> getDisabledSlots() { var slots = java.util.EnumSet.noneOf(EquipmentSlot.class); for (var slot : EquipmentSlot.values()) if (isSlotDisabled(slot)) slots.add(slot); return slots; }
    @Override public void setDisabledSlots(EquipmentSlot... slots) { int flags = 0; for (var slot : java.util.Objects.requireNonNull(slots)) if (java.util.Objects.requireNonNull(slot) != EquipmentSlot.OFF_HAND) for (var lock : LockType.values()) flags |= lock(slot, lock); requireWritable(); state().meldrite$setDisabledSlots(flags); }
    @Override public void addDisabledSlots(EquipmentSlot... slots) { var disabled = getDisabledSlots(); java.util.Collections.addAll(disabled, slots); setDisabledSlots(disabled.toArray(EquipmentSlot[]::new)); }
    @Override public void removeDisabledSlots(EquipmentSlot... slots) { var disabled = getDisabledSlots(); for (var slot : slots) disabled.remove(slot); setDisabledSlots(disabled.toArray(EquipmentSlot[]::new)); }
    @Override public boolean isSlotDisabled(EquipmentSlot slot) { return state().meldrite$isSlotDisabled(CraftEntityEquipment.nativeSlot(slot)); }
    @Override public boolean canMove() { return state().meldrite$canMove(); }
    @Override public void setCanMove(boolean move) { requireWritable(); state().meldrite$setCanMove(move); }
    @Override public boolean canTick() { return state().meldrite$canTick(); }
    @Override public void setCanTick(boolean tick) { requireWritable(); state().meldrite$setCanTick(tick); }
}
