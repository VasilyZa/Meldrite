package org.bukkit.craftbukkit.v1_20_R1.attribute;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;

public final class CraftAttributeInstance implements org.bukkit.attribute.AttributeInstance {
    private final CraftLivingEntity owner;
    private final Attribute attribute;
    private final net.minecraft.entity.attribute.EntityAttributeInstance handle;
    public CraftAttributeInstance(CraftLivingEntity owner, Attribute attribute, net.minecraft.entity.attribute.EntityAttributeInstance handle) { this.owner = owner; this.attribute = attribute; this.handle = handle; }
    public CraftAttributeInstance(net.minecraft.entity.attribute.EntityAttributeInstance handle, Attribute attribute) { this(null, java.util.Objects.requireNonNull(attribute), java.util.Objects.requireNonNull(handle)); }
    public net.minecraft.entity.attribute.EntityAttributeInstance getHandle() { if (owner != null) owner.getHandle(); return handle; }
    private void writable() { if (owner != null) owner.requireWritable(); else dev.meldrite.v1201.compat.NativeRecipes.requireThread(); }
    public static AttributeModifier convert(net.minecraft.entity.attribute.EntityAttributeModifier modifier) { return new AttributeModifier(modifier.getId(), modifier.getName(), modifier.getValue(), AttributeModifier.Operation.values()[modifier.getOperation().ordinal()]); }
    public static net.minecraft.entity.attribute.EntityAttributeModifier convert(AttributeModifier modifier) { return nativeModifier(modifier); }
    @Override public Attribute getAttribute() { return attribute; }
    @Override public double getBaseValue() { return getHandle().getBaseValue(); }
    @Override public void setBaseValue(double value) { if (!Double.isFinite(value)) throw new IllegalArgumentException("nonfinite attribute value"); writable(); getHandle().setBaseValue(value); }
    @Override public java.util.Collection<AttributeModifier> getModifiers() { return getHandle().getModifiers().stream().map(modifier -> new AttributeModifier(modifier.getId(), modifier.getName(), modifier.getValue(), AttributeModifier.Operation.values()[modifier.getOperation().ordinal()])).toList(); }
    private static net.minecraft.entity.attribute.EntityAttributeModifier nativeModifier(AttributeModifier modifier) { java.util.Objects.requireNonNull(modifier); return new net.minecraft.entity.attribute.EntityAttributeModifier(modifier.getUniqueId(), modifier.getName(), modifier.getAmount(), net.minecraft.entity.attribute.EntityAttributeModifier.Operation.values()[modifier.getOperation().ordinal()]); }
    @Override public void addModifier(AttributeModifier modifier) { writable(); getHandle().addPersistentModifier(nativeModifier(modifier)); }
    @Override public void addTransientModifier(AttributeModifier modifier) { writable(); getHandle().addTemporaryModifier(nativeModifier(modifier)); }
    @Override public void removeModifier(AttributeModifier modifier) { writable(); getHandle().removeModifier(java.util.Objects.requireNonNull(modifier).getUniqueId()); }
    @Override public double getValue() { return getHandle().getValue(); }
    @Override public double getDefaultValue() { return getHandle().getAttribute().getDefaultValue(); }
}
