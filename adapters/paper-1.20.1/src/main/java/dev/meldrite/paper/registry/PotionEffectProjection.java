package dev.meldrite.paper.registry;

import java.util.*;
import java.util.function.IntFunction;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

/** Exposes the native effect registry through Bukkit's legacy static registry. */
public final class PotionEffectProjection {
    public record Modifier(String attributeKey, UUID id, String name, double amount,
                           AttributeModifier.Operation operation, EquipmentSlot slot) {
        public Modifier { Objects.requireNonNull(attributeKey); Objects.requireNonNull(id); Objects.requireNonNull(name); Objects.requireNonNull(operation); }
    }
    public record Definition(int id, String namespace, String key, String legacyName, double durationModifier,
                             boolean instant, int color, PotionEffectType.Category category,
                             String translationKey, List<Modifier> modifiers) {
        public Definition {
            Objects.requireNonNull(namespace); Objects.requireNonNull(key); Objects.requireNonNull(legacyName); Objects.requireNonNull(category); Objects.requireNonNull(translationKey);
            modifiers = List.copyOf(modifiers);
            if (id <= 0) throw new IllegalArgumentException("Effect id must be positive");
            Color.fromRGB(color);
        }
    }
    @FunctionalInterface public interface ModifierAmount {
        double amount(int effectId, String attributeKey, int amplifier);
    }
    private PotionEffectProjection() { }

    public static org.bukkit.Registry<PotionEffectType> registry() {
        return new org.bukkit.Registry<>() {
            @Override public PotionEffectType get(NamespacedKey key) { return PotionEffectType.getByKey(Objects.requireNonNull(key)); }
            @Override public Iterator<PotionEffectType> iterator() { return stream().iterator(); }
            @Override public java.util.stream.Stream<PotionEffectType> stream() { return Arrays.stream(PotionEffectType.values()).filter(Objects::nonNull); }
        };
    }

    public static void install(Collection<Definition> definitions, ModifierAmount amounts) {
        Map<Integer, List<Modifier>> modifiers = new HashMap<>();
        for (Definition definition : definitions) modifiers.put(definition.id(), definition.modifiers());
        install(definitions, modifiers::get, amounts);
    }

    public static void install(Collection<Definition> definitions, IntFunction<List<Modifier>> modifiers, ModifierAmount amounts) {
        Objects.requireNonNull(definitions); Objects.requireNonNull(modifiers); Objects.requireNonNull(amounts);
        Set<Integer> ids = new HashSet<>(); Set<NamespacedKey> keys = new HashSet<>();
        List<ProjectedEffect> effects = new ArrayList<>();
        for (Definition definition : definitions) {
            NamespacedKey key = new NamespacedKey(definition.namespace(), definition.key());
            if (!ids.add(definition.id()) || !keys.add(key)) throw new IllegalArgumentException("DUPLICATE_EFFECT_DEFINITION: " + key);
            PotionEffectType current = PotionEffectType.getById(definition.id());
            if (current != null) {
                if (current instanceof ProjectedEffect projected && projected.definition.equals(definition)) continue;
                throw new IllegalStateException("POTION_EFFECT_ALREADY_REGISTERED: " + definition.id());
            }
            effects.add(new ProjectedEffect(definition, modifiers, amounts));
        }
        effects.forEach(PotionEffectType::registerPotionEffectType);
    }

    private static final class ProjectedEffect extends PotionEffectType {
        private final Definition definition;
        private final IntFunction<List<Modifier>> modifiers;
        private final ModifierAmount amounts;
        private ProjectedEffect(Definition definition, IntFunction<List<Modifier>> modifiers, ModifierAmount amounts) {
            super(definition.id(), new NamespacedKey(definition.namespace(), definition.key()));
            this.definition = definition; this.modifiers = modifiers; this.amounts = amounts;
        }
        private List<Modifier> modifiers() { return List.copyOf(Objects.requireNonNull(modifiers.apply(definition.id()), "native modifiers")); }
        @Override public double getDurationModifier() { return definition.durationModifier(); }
        @Override public String getName() { return definition.legacyName(); }
        @Override public boolean isInstant() { return definition.instant(); }
        @Override public Color getColor() { return Color.fromRGB(definition.color()); }
        @Override public Category getEffectCategory() { return definition.category(); }
        @Override public String translationKey() { return definition.translationKey(); }
        private Attribute attribute(String key) {
            return Arrays.stream(Attribute.values()).filter(value -> value.getKey().toString().equals(key)).findFirst()
                    .orElseThrow(() -> new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_ATTRIBUTE: " + key));
        }
        @Override public Map<Attribute, AttributeModifier> getEffectAttributes() {
            Map<Attribute, AttributeModifier> result = new LinkedHashMap<>();
            for (Modifier modifier : modifiers()) result.put(attribute(modifier.attributeKey()), new AttributeModifier(modifier.id(), modifier.name(), modifier.amount(), modifier.operation(), modifier.slot()));
            return Map.copyOf(result);
        }
        @Override public double getAttributeModifierAmount(Attribute attribute, int amplifier) {
            Objects.requireNonNull(attribute);
            if (amplifier < 0) throw new IllegalArgumentException("Effect amplifier must be non-negative");
            String key = attribute.getKey().toString();
            if (modifiers().stream().noneMatch(value -> value.attributeKey().equals(key))) throw new IllegalArgumentException("Attribute is absent from effect: " + key);
            return amounts.amount(definition.id(), key, amplifier);
        }
    }
}
