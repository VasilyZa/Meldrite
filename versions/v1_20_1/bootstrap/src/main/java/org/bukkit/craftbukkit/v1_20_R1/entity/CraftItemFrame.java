package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.ItemFrameEntityPaperAccessMixin;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
public class CraftItemFrame extends CraftHanging implements org.bukkit.entity.ItemFrame {
    public CraftItemFrame(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ItemFrameEntity getHandle() { return (ItemFrameEntity) super.getHandle(); }
    private ItemFrameEntityPaperAccessMixin access() { return (ItemFrameEntityPaperAccessMixin) getHandle(); }
    @Override public boolean setFacingDirection(BlockFace face, boolean force) {
        net.minecraft.util.math.Direction direction;
        try { direction = net.minecraft.util.math.Direction.valueOf(java.util.Objects.requireNonNull(face, "face").name()); } catch (IllegalArgumentException invalid) { throw new IllegalArgumentException("Invalid item frame face: " + face, invalid); }
        if (!setDirection(direction, force)) return false; update(); getHandle().getWorld().updateComparators(getHandle().getDecorationBlockPos(), net.minecraft.block.Blocks.AIR); return true;
    }
    @Override public org.bukkit.inventory.ItemStack getItem() { return CraftItemStack.asBukkitCopy(getHandle().getHeldItemStack()); }
    @Override public void setItem(org.bukkit.inventory.ItemStack item) { setItem(item, true); }
    @Override public void setItem(org.bukkit.inventory.ItemStack item, boolean playSound) {
        delegate.requireWritable(); dev.meldrite.v1201.compat.NativeItemFrameItems.set(getHandle(), CraftItemStack.asNMSCopy(item), playSound);
    }
    @Override public float getItemDropChance() { return access().meldrite$getDropChance(); }
    @Override public void setItemDropChance(float chance) { if (!(chance >= 0 && chance <= 1)) throw new IllegalArgumentException("Chance outside [0,1]"); delegate.requireWritable(); access().meldrite$setDropChance(chance); }
    @Override public Rotation getRotation() { return toBukkitRotation(getHandle().getRotation()); }
    Rotation toBukkitRotation(int value) { if (value < 0 || value > 7) throw new AssertionError("Unknown rotation: " + value); return Rotation.values()[value]; }
    static int toInteger(Rotation rotation) { return java.util.Objects.requireNonNull(rotation, "rotation").ordinal(); }
    @Override public void setRotation(Rotation value) { delegate.requireWritable(); getHandle().setRotation(toInteger(value)); }
    @Override public boolean isVisible() { return !getHandle().isInvisible(); }
    @Override public void setVisible(boolean value) { delegate.requireWritable(); getHandle().setInvisible(!value); }
    @Override public boolean isFixed() { return access().meldrite$isFixed(); }
    @Override public void setFixed(boolean value) { delegate.requireWritable(); access().meldrite$setFixed(value); }
}
