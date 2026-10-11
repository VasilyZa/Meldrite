package dev.meldrite.paper.facade;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/** Persistent data backed by the actual PublicBukkitValues item compound. */
final class ItemPersistentDataContainer implements PersistentDataContainer {
    private final Supplier<Map<String, Object>> values;
    private final PersistentDataAdapterContext context = () -> {
        Map<String, Object> data = new LinkedHashMap<>();
        return new ItemPersistentDataContainer(() -> data);
    };

    ItemPersistentDataContainer(Supplier<Map<String, Object>> values) { this.values = values; }
    @Override public <T, Z> void set(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        Objects.requireNonNull(key); Objects.requireNonNull(type); Objects.requireNonNull(value);
        Object primitive = type.toPrimitive(value, context);
        if (primitive instanceof PersistentDataContainer container) primitive = containerValues(container);
        if (primitive instanceof PersistentDataContainer[] containers) {
            List<Object> result = new ArrayList<>();
            for (PersistentDataContainer container : containers) {
                result.add(containerValues(container));
            }
            primitive = result;
        }
        values.get().put(key.toString(), ItemNbtData.copyValue(primitive));
    }
    @Override public <T, Z> boolean has(NamespacedKey key, PersistentDataType<T, Z> type) { return primitive(key, type) != null; }
    @Override public <T, Z> Z get(NamespacedKey key, PersistentDataType<T, Z> type) {
        T value = primitive(key, type);
        if (value == null && has(key)) throw new IllegalArgumentException("The stored tag cannot store " + type.getPrimitiveType().getSimpleName());
        return value == null ? null : type.fromPrimitive(value, context);
    }
    @SuppressWarnings("unchecked") private <T, Z> T primitive(NamespacedKey key, PersistentDataType<T, Z> type) {
        Object value = values.get().get(Objects.requireNonNull(key).toString());
        if (value == null) return null;
        Class<T> primitive = Objects.requireNonNull(type).getPrimitiveType();
        if (primitive == PersistentDataContainer.class && value instanceof Map<?, ?> map) {
            Map<String, Object> data = ItemNbtData.copy(ItemNbtData.compound(map));
            return (T) new ItemPersistentDataContainer(() -> data);
        }
        if (primitive == PersistentDataContainer[].class && value instanceof List<?> list) {
            List<PersistentDataContainer> result = new ArrayList<>();
            for (Object entry : list) {
                // Leaf ListTag#getCompound projects non-compound elements as empty compounds.
                Map<String, Object> data = ItemNbtData.copy(ItemNbtData.compound(entry));
                result.add(new ItemPersistentDataContainer(() -> data));
            }
            return (T) result.toArray(PersistentDataContainer[]::new);
        }
        return primitive.isInstance(value) ? (T) ItemNbtData.copyValue(value) : null;
    }
    @Override public <T, Z> Z getOrDefault(NamespacedKey key, PersistentDataType<T, Z> type, Z defaultValue) { Z value = get(key, type); return value == null ? defaultValue : value; }
    @Override public Set<NamespacedKey> getKeys() { Set<NamespacedKey> result = new LinkedHashSet<>(); values.get().keySet().forEach(key -> { NamespacedKey parsed = NamespacedKey.fromString(key); if (parsed != null) result.add(parsed); }); return Set.copyOf(result); }
    @Override public void remove(NamespacedKey key) { values.get().remove(Objects.requireNonNull(key).toString()); }
    @Override public boolean isEmpty() { return values.get().isEmpty(); }
    @Override public PersistentDataAdapterContext getAdapterContext() { return context; }
    @Override public boolean has(NamespacedKey key) { return values.get().containsKey(Objects.requireNonNull(key).toString()); }
    @Override public byte[] serializeToBytes() throws IOException { return ItemNbtData.encodeUncompressed(values.get()); }
    @Override public void readFromBytes(byte[] bytes, boolean clear) throws IOException {
        if (clear) values.get().clear();
        values.get().putAll(ItemNbtData.decodeUncompressed(bytes));
    }
    private static Map<String, Object> containerValues(PersistentDataContainer container) {
        if (container instanceof ItemPersistentDataContainer own) return own.values.get();
        try { return ItemNbtData.decodeUncompressed(container.serializeToBytes()); }
        catch (IOException failure) { throw new UncheckedIOException("Cannot read persistent container NBT", failure); }
    }
}
