package org.bukkit.craftbukkit.v1_20_R1.persistence;

import java.io.IOException;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

/**
 * Persistent data container that records mutations made by its Bukkit owner.
 *
 * <p>Chunk and block-entity serializers use this bit to avoid writing an
 * otherwise unchanged BukkitValues compound.  The actual values remain in
 * the same native NBT map as the base container.</p>
 */
public class DirtyCraftPersistentDataContainer extends CraftPersistentDataContainer {
    private boolean dirty;

    public DirtyCraftPersistentDataContainer(CraftPersistentDataTypeRegistry registry) {
        super(registry);
    }

    public DirtyCraftPersistentDataContainer(Map<String, NbtElement> customTags,
                                             CraftPersistentDataTypeRegistry registry) {
        super(customTags, registry);
    }

    public boolean dirty() {
        return dirty;
    }

    public void dirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public <T, Z> void set(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        super.set(key, type, value);
        dirty = true;
    }

    @Override
    public void remove(NamespacedKey key) {
        super.remove(key);
        dirty = true;
    }

    @Override
    public void clear() {
        super.clear();
        dirty = true;
    }

    @Override
    public void readFromBytes(byte[] bytes, boolean clear) throws IOException {
        super.readFromBytes(bytes, clear);
        dirty = true;
    }

    @Override
    public void put(String key, NbtElement tag) {
        super.put(key, tag);
        dirty = true;
    }

    @Override
    public void putAll(Map<String, NbtElement> tags) {
        super.putAll(tags);
        dirty = true;
    }

    @Override
    public void putAll(NbtCompound compound) {
        super.putAll(compound);
        dirty = true;
    }
}
