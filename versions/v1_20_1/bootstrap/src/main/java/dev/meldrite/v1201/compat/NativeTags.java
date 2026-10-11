package dev.meldrite.v1201.compat;

import java.util.Objects;
import java.util.function.BiFunction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.craftbukkit.v1_20_R1.tag.*;

public final class NativeTags {
    private NativeTags() { }
    private record Binding<N, B extends Keyed>(Registry<N> registry, BiFunction<Registry<N>, TagKey<N>, Tag<B>> projection) {
        Tag<B> get(NamespacedKey key) {
            var nativeKey = TagKey.of(registry.getKey(), new Identifier(key.getNamespace(), key.getKey()));
            return registry.getEntryList(nativeKey).isPresent() ? projection.apply(registry, nativeKey) : null;
        }
        Iterable<Tag<B>> all() { return registry.streamTags().map(key -> projection.apply(registry, key)).toList(); }
    }
    @SuppressWarnings("unchecked")
    private static <T extends Keyed> Binding<?, T> binding(String registry, Class<T> type) {
        Objects.requireNonNull(registry); Objects.requireNonNull(type);
        Binding<?, ?> value = switch (registry) {
            case Tag.REGISTRY_BLOCKS -> { require(type, org.bukkit.Material.class); yield new Binding<>(Registries.BLOCK, CraftBlockTag::new); }
            case Tag.REGISTRY_ITEMS -> { require(type, org.bukkit.Material.class); yield new Binding<>(Registries.ITEM, CraftItemTag::new); }
            case Tag.REGISTRY_FLUIDS -> { require(type, org.bukkit.Fluid.class); yield new Binding<>(Registries.FLUID, CraftFluidTag::new); }
            case Tag.REGISTRY_ENTITY_TYPES -> { require(type, org.bukkit.entity.EntityType.class); yield new Binding<>(Registries.ENTITY_TYPE, CraftEntityTag::new); }
            case Tag.REGISTRY_GAME_EVENTS -> { require(type, org.bukkit.GameEvent.class); yield new Binding<>(Registries.GAME_EVENT, CraftGameEventTag::new); }
            default -> throw new IllegalArgumentException("Unknown tag registry: " + registry);
        };
        return (Binding<?, T>) value;
    }
    private static void require(Class<?> actual, Class<?> expected) {
        if (actual != expected) throw new IllegalArgumentException("Tag registry requires " + expected.getName() + ", got " + actual.getName());
    }
    public static <T extends Keyed> Tag<T> get(String registry, NamespacedKey key, Class<T> type) {
        return binding(registry, type).get(Objects.requireNonNull(key));
    }
    public static <T extends Keyed> Iterable<Tag<T>> all(String registry, Class<T> type) { return binding(registry, type).all(); }
}
