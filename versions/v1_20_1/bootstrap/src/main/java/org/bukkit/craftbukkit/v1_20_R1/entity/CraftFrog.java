package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.FrogEntity;

public class CraftFrog extends CraftAnimals implements org.bukkit.entity.Frog {
    public CraftFrog(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public FrogEntity getHandle() { return (FrogEntity) super.getHandle(); }
    @Override public org.bukkit.entity.Entity getTongueTarget() { return getHandle().getFrogTarget().map(dev.meldrite.v1201.compat.NativeEntityExtensions::bukkit).orElse(null); }
    @Override public void setTongueTarget(org.bukkit.entity.Entity target) {
        requireWritable(); if (target == null) getHandle().clearFrogTarget();
        else { if (!(target instanceof CraftEntity craft) || craft.getWorld() != getWorld()) throw new IllegalArgumentException("Tongue target must be a native entity in the same world"); getHandle().setFrogTarget(craft.getHandle()); }
    }
    @Override public Variant getVariant() {
        var id = net.minecraft.registry.Registries.FROG_VARIANT.getId(getHandle().getVariant());
        var key = org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.fromMinecraft(id);
        for (var variant : Variant.values()) if (variant.getKey().equals(key)) return variant;
        throw new IllegalStateException("Frog variant has no Bukkit projection: " + id);
    }
    @Override public void setVariant(Variant variant) { requireWritable(); getHandle().setVariant(net.minecraft.registry.Registries.FROG_VARIANT.get(org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(java.util.Objects.requireNonNull(variant, "variant").getKey()))); }
}
