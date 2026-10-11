package org.bukkit.craftbukkit.v1_20_R1.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CraftPersistentDataContainer implements PersistentDataContainer {
    private final Map<String, NbtElement> customDataTags = new HashMap<>();
    private final CraftPersistentDataTypeRegistry registry;
    private final CraftPersistentDataAdapterContext adapterContext;

    public CraftPersistentDataContainer(CraftPersistentDataTypeRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
        this.adapterContext = new CraftPersistentDataAdapterContext(registry);
    }

    public CraftPersistentDataContainer(Map<String, NbtElement> customTags, CraftPersistentDataTypeRegistry registry) {
        this(registry);
        customDataTags.putAll(customTags);
    }

    @Override public <T, Z> void set(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        require(key, "key"); require(type, "type"); require(value, "value");
        customDataTags.put(key.toString(), registry.wrap(type.getPrimitiveType(), type.toPrimitive(value, adapterContext)));
    }

    @Override public <T, Z> boolean has(NamespacedKey key, PersistentDataType<T, Z> type) {
        require(key, "key"); require(type, "type");
        NbtElement tag = customDataTags.get(key.toString());
        return tag != null && registry.isInstanceOf(type.getPrimitiveType(), tag);
    }

    @Override public boolean has(NamespacedKey key) {
        require(key, "key");
        return customDataTags.containsKey(key.toString());
    }

    @Override public <T, Z> Z get(NamespacedKey key, PersistentDataType<T, Z> type) {
        require(key, "key"); require(type, "type");
        NbtElement tag = customDataTags.get(key.toString());
        return tag == null ? null : type.fromPrimitive(registry.extract(type.getPrimitiveType(), tag), adapterContext);
    }

    @Override public <T, Z> Z getOrDefault(NamespacedKey key, PersistentDataType<T, Z> type, Z defaultValue) {
        Z value = get(key, type);
        return value == null ? defaultValue : value;
    }

    @Override public Set<NamespacedKey> getKeys() {
        Set<NamespacedKey> keys = new HashSet<>();
        for (String key : customDataTags.keySet()) {
            String[] parts = key.split(":", 2);
            if (parts.length == 2) keys.add(new NamespacedKey(parts[0], parts[1]));
        }
        return keys;
    }

    @Override public void remove(NamespacedKey key) { require(key, "key"); customDataTags.remove(key.toString()); }
    @Override public boolean isEmpty() { return customDataTags.isEmpty(); }
    @Override public PersistentDataAdapterContext getAdapterContext() { return adapterContext; }
    public void clear() { customDataTags.clear(); }

    public NbtCompound toTagCompound() {
        NbtCompound compound = new NbtCompound();
        customDataTags.forEach(compound::put);
        return compound;
    }

    public void put(String key, NbtElement tag) { customDataTags.put(key, tag); }
    public void putAll(Map<String, NbtElement> tags) { customDataTags.putAll(tags); }
    public void putAll(NbtCompound compound) {
        for (String key : compound.getKeys()) customDataTags.put(key, compound.get(key));
    }
    public Map<String, NbtElement> getRaw() { return customDataTags; }
    public CraftPersistentDataTypeRegistry getDataTagTypeRegistry() { return registry; }
    public Map<String, NbtElement> getTagsCloned() {
        Map<String, NbtElement> cloned = new HashMap<>();
        customDataTags.forEach((key, tag) -> cloned.put(key, tag.copy()));
        return cloned;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        customDataTags.forEach((key, tag) -> serialized.put(key, serializeTag(tag)));
        return serialized;
    }

    private static Object serializeTag(NbtElement tag) {
        if (tag instanceof NbtCompound compound) {
            Map<String, Object> values = new HashMap<>();
            for (String key : compound.getKeys()) values.put(key, serializeTag(compound.get(key)));
            return values;
        }
        if (tag instanceof NbtList list) {
            var values = new ArrayList<Object>(list.size());
            for (NbtElement element : list) values.add(serializeTag(element));
            return values;
        }
        if (tag instanceof NbtString) return tag.asString();
        if (tag instanceof NbtInt) return tag + "i";
        return tag.toString();
    }

    @Override public byte[] serializeToBytes() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(output)) { NbtIo.write(toTagCompound(), data); }
        return output.toByteArray();
    }

    @Override public void readFromBytes(byte[] bytes, boolean clear) throws IOException {
        if (clear) clear();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) { putAll(NbtIo.read(input)); }
    }

    @Override public boolean equals(Object other) {
        return other instanceof CraftPersistentDataContainer container && customDataTags.equals(container.customDataTags);
    }
    @Override public int hashCode() { return 3 + customDataTags.hashCode(); }
    private static void require(Object value, String name) {
        if (value == null) throw new IllegalArgumentException("The persistent data " + name + " cannot be null");
    }
}
