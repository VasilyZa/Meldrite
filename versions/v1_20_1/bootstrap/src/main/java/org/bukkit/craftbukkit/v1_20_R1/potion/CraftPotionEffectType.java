package org.bukkit.craftbukkit.v1_20_R1.potion;

import java.util.Map;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import org.bukkit.Color;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_20_R1.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v1_20_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.potion.PotionEffectType;

public class CraftPotionEffectType extends PotionEffectType {
    private final StatusEffect handle;
    public CraftPotionEffectType(StatusEffect handle) { super(Registries.STATUS_EFFECT.getRawId(handle), CraftNamespacedKey.fromMinecraft(Registries.STATUS_EFFECT.getId(handle))); this.handle = java.util.Objects.requireNonNull(handle); }
    public StatusEffect getHandle() { return handle; }
    @Override public String getName() {
        var projected = PotionEffectType.getByKey(getKey());
        return projected != null && projected != this ? projected.getName() : "UNKNOWN_EFFECT_TYPE_" + getId();
    }
    @Override public double getDurationModifier() { return 1; }
    @Override public boolean isInstant() { return handle.isInstant(); }
    @Override public Color getColor() { return Color.fromRGB(handle.getColor()); }
    @Override public String translationKey() { return handle.getTranslationKey(); }
    @Override public Category getEffectCategory() { return fromNMS(handle.getCategory()); }
    public static Category fromNMS(StatusEffectCategory category) { return Category.valueOf(java.util.Objects.requireNonNull(category).name()); }
    @Override public Map<Attribute, AttributeModifier> getEffectAttributes() {
        var result = new java.util.HashMap<Attribute, AttributeModifier>();
        handle.getAttributeModifiers().forEach((attribute, modifier) -> {
            var projected = CraftAttributeMap.fromMinecraft(Registries.ATTRIBUTE.getId(attribute).toString());
            if (projected == null) throw new IllegalArgumentException("Unprojected effect attribute: " + Registries.ATTRIBUTE.getId(attribute));
            result.put(projected, CraftAttributeInstance.convert(modifier));
        });
        return Map.copyOf(result);
    }
    @Override public double getAttributeModifierAmount(Attribute attribute, int amplifier) {
        if (amplifier < 0) throw new IllegalArgumentException("Negative amplifier");
        var modifier = handle.getAttributeModifiers().get(CraftAttributeMap.toMinecraft(attribute));
        if (modifier == null) throw new IllegalArgumentException("Attribute is absent from effect");
        return handle.adjustModifierAmount(amplifier, modifier);
    }
}
