package dev.meldrite.v1201.compat;

import dev.meldrite.paper.registry.PotionEffectProjection;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.potion.PotionEffectType;

public final class NativePotionProjection {
    private static final String[] LEGACY_NAMES = { "", "SPEED", "SLOW", "FAST_DIGGING", "SLOW_DIGGING",
            "INCREASE_DAMAGE", "HEAL", "HARM", "JUMP", "CONFUSION", "REGENERATION", "DAMAGE_RESISTANCE",
            "FIRE_RESISTANCE", "WATER_BREATHING", "INVISIBILITY", "BLINDNESS", "NIGHT_VISION", "HUNGER",
            "WEAKNESS", "POISON", "WITHER", "HEALTH_BOOST", "ABSORPTION", "SATURATION", "GLOWING",
            "LEVITATION", "LUCK", "UNLUCK", "SLOW_FALLING", "CONDUIT_POWER", "DOLPHINS_GRACE", "BAD_OMEN",
            "HERO_OF_THE_VILLAGE", "DARKNESS" };
    private NativePotionProjection() { }
    public static void install() {
        List<PotionEffectProjection.Definition> definitions = new ArrayList<>();
        for (StatusEffect effect : Registries.STATUS_EFFECT) {
            int id = Registries.STATUS_EFFECT.getRawId(effect);
            Identifier key = Registries.STATUS_EFFECT.getId(effect);
            String name = id > 0 && id < LEGACY_NAMES.length ? LEGACY_NAMES[id] : "UNKNOWN_EFFECT_TYPE_" + id;
            PotionEffectType.Category category = switch (effect.getCategory()) {
                case BENEFICIAL -> PotionEffectType.Category.BENEFICIAL;
                case HARMFUL -> PotionEffectType.Category.HARMFUL;
                case NEUTRAL -> PotionEffectType.Category.NEUTRAL;
            };
            // CraftPotionEffectType in Leaf 1.20.1 uses this fixed duration modifier.
            definitions.add(new PotionEffectProjection.Definition(id, key.getNamespace(), key.getPath(), name, 1.0,
                    effect.isInstant(), effect.getColor(), category, effect.getTranslationKey(), modifiers(id)));
        }
        PotionEffectProjection.install(definitions, NativePotionProjection::modifiers, NativePotionProjection::amount);
    }

    private static StatusEffect effect(int id) {
        return Registries.STATUS_EFFECT.getEntry(id).orElseThrow(() -> new IllegalArgumentException("UNKNOWN_NATIVE_EFFECT: " + id)).value();
    }

    private static List<PotionEffectProjection.Modifier> modifiers(int id) {
        List<PotionEffectProjection.Modifier> modifiers = new ArrayList<>();
        effect(id).getAttributeModifiers().forEach((attribute, modifier) -> {
            Identifier key = Registries.ATTRIBUTE.getId(attribute);
            if (key == null) throw new IllegalStateException("UNREGISTERED_EFFECT_ATTRIBUTE: " + attribute);
            AttributeModifier.Operation operation = switch (modifier.getOperation()) {
                case ADDITION -> AttributeModifier.Operation.ADD_NUMBER;
                case MULTIPLY_BASE -> AttributeModifier.Operation.ADD_SCALAR;
                case MULTIPLY_TOTAL -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            };
            modifiers.add(new PotionEffectProjection.Modifier(key.toString(), modifier.getId(), modifier.getName(), modifier.getValue(), operation, null));
        });
        return List.copyOf(modifiers);
    }

    private static double amount(int id, String attributeKey, int amplifier) {
        StatusEffect effect = effect(id);
        EntityAttribute attribute = Registries.ATTRIBUTE.getOrEmpty(new Identifier(attributeKey))
                .orElseThrow(() -> new IllegalArgumentException("UNKNOWN_NATIVE_ATTRIBUTE: " + attributeKey));
        EntityAttributeModifier modifier = effect.getAttributeModifiers().get(attribute);
        if (modifier == null) throw new IllegalArgumentException("Attribute is absent from effect: " + attributeKey);
        return effect.adjustModifierAmount(amplifier, modifier);
    }
}
