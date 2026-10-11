package io.papermc.paper.entity;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;

public interface PaperBucketable extends Bucketable {
    net.minecraft.entity.Bucketable getHandle();
    @Override default boolean isFromBucket() { return getHandle().isFromBucket(); }
    @Override default void setFromBucket(boolean fromBucket) { NativeEntityExtensions.requireThread((net.minecraft.entity.Entity) getHandle()); getHandle().setFromBucket(fromBucket); }
    @Override default org.bukkit.inventory.ItemStack getBaseBucketItem() { return CraftItemStack.asBukkitCopy(getHandle().getBucketItem()); }
    @Override default org.bukkit.Sound getPickupSound() {
        var id = getHandle().getBucketFillSound().getId();
        return org.bukkit.Registry.SOUNDS.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
    }
}
