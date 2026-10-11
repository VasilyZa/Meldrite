package org.bukkit.craftbukkit.v1_20_R1.attribute;

import java.util.Map;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

public class CraftAttributeMap implements org.bukkit.attribute.Attributable {
    private final AttributeContainer handle;
    private static final Map<String, String> LEGACY = Map.ofEntries(
        Map.entry("generic.maxHealth", "generic.max_health"), Map.entry("Max Health", "generic.max_health"),
        Map.entry("generic.followRange", "generic.follow_range"), Map.entry("Follow Range", "generic.follow_range"),
        Map.entry("generic.knockbackResistance", "generic.knockback_resistance"), Map.entry("Knockback Resistance", "generic.knockback_resistance"),
        Map.entry("generic.movementSpeed", "generic.movement_speed"), Map.entry("Movement Speed", "generic.movement_speed"),
        Map.entry("generic.flyingSpeed", "generic.flying_speed"), Map.entry("Flying Speed", "generic.flying_speed"),
        Map.entry("generic.attackDamage", "generic.attack_damage"), Map.entry("generic.attackKnockback", "generic.attack_knockback"),
        Map.entry("generic.attackSpeed", "generic.attack_speed"), Map.entry("generic.armorToughness", "generic.armor_toughness"),
        Map.entry("horse.jumpStrength", "horse.jump_strength"), Map.entry("Jump Strength", "horse.jump_strength"),
        Map.entry("zombie.spawnReinforcements", "zombie.spawn_reinforcements"), Map.entry("Spawn Reinforcements Chance", "zombie.spawn_reinforcements"));
    public CraftAttributeMap(AttributeContainer handle) { this.handle = java.util.Objects.requireNonNull(handle); }
    public static String convertIfNeeded(String name) {
        if (name == null) return null;
        name = LEGACY.getOrDefault(name, name);
        return Identifier.isValid(name) ? name : null;
    }
    public static EntityAttribute toMinecraft(Attribute attribute) {
        var key = java.util.Objects.requireNonNull(attribute).getKey();
        return Registries.ATTRIBUTE.getOrEmpty(new Identifier(key.getNamespace(), key.getKey())).orElseThrow(() -> new IllegalArgumentException("Unknown native attribute: " + key));
    }
    public static Attribute fromMinecraft(String name) {
        name = convertIfNeeded(name); if (name == null) return null;
        var id = new Identifier(name);
        for (var attribute : Attribute.values()) if (attribute.getKey().toString().equals(id.toString())) return attribute;
        return null;
    }
    @Override public AttributeInstance getAttribute(Attribute attribute) {
        var instance = handle.getCustomInstance(toMinecraft(attribute));
        return instance == null ? null : new CraftAttributeInstance(instance, attribute);
    }
    @Override public void registerAttribute(Attribute attribute) {
        dev.meldrite.v1201.compat.NativeRecipes.requireThread();
        ((dev.meldrite.v1201.compat.NativeAttributeRegistration) handle).registerAttribute(toMinecraft(attribute));
    }
}
