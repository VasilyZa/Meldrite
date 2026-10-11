package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.CatEntity;

public class CraftCat extends CraftTameableAnimal implements org.bukkit.entity.Cat {
    public CraftCat(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public CatEntity getHandle() { return (CatEntity) super.getHandle(); }
    @Override public Type getCatType() {
        var id = net.minecraft.registry.Registries.CAT_VARIANT.getId(getHandle().getVariant());
        var key = org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.fromMinecraft(id);
        for (var type : Type.values()) if (type.getKey().equals(key)) return type;
        throw new IllegalStateException("Cat variant has no Bukkit projection: " + id);
    }
    @Override public void setCatType(Type type) { requireWritable(); getHandle().setVariant(net.minecraft.registry.Registries.CAT_VARIANT.get(org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(java.util.Objects.requireNonNull(type, "type").getKey()))); }
    @Override public org.bukkit.DyeColor getCollarColor() { return org.bukkit.DyeColor.getByWoolData((byte) getHandle().getCollarColor().getId()); }
    @Override public void setCollarColor(org.bukkit.DyeColor color) { requireWritable(); getHandle().setCollarColor(net.minecraft.util.DyeColor.byId(java.util.Objects.requireNonNull(color, "color").getWoolData())); }
    @Override public void setLyingDown(boolean value) { requireWritable(); getHandle().setInSleepingPose(value); }
    @Override public boolean isLyingDown() { return getHandle().isInSleepingPose(); }
    @Override public void setHeadUp(boolean value) { requireWritable(); getHandle().setHeadDown(value); }
    @Override public boolean isHeadUp() { return getHandle().isHeadDown(); }
}
