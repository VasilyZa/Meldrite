package org.bukkit.craftbukkit.v1_20_R1.persistence;

import com.google.common.primitives.Primitives;
import dev.meldrite.v1201.compat.NativeItemMetadata;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtIo;
import org.bukkit.persistence.PersistentDataContainer;

/** The Paper primitive-type contract over the actual native NBT tags. */
public final class CraftPersistentDataTypeRegistry {
    /** Generic adapter shape retained for plugins which inspect the registry reflectively. */
    final class TagAdapter<T, Z extends NbtElement> {
        private final Class<T> primitiveType;
        private final Class<Z> nbtType;
        private final Function<T, Z> builder;
        private final Function<Z, T> extractor;
        TagAdapter(Class<T> primitiveType, Class<Z> nbtType, Function<T, Z> builder, Function<Z, T> extractor) {
            this.primitiveType = primitiveType;
            this.nbtType = nbtType;
            this.builder = builder;
            this.extractor = extractor;
        }
        boolean isInstance(NbtElement tag) { return nbtType.isInstance(tag); }
        Z build(Object value) { return builder.apply(primitiveType.cast(value)); }
        T extract(NbtElement tag) { return extractor.apply(nbtType.cast(tag)); }
    }
    public <T> NbtElement wrap(Class<T> type, T value) {
        Class<?> primitive = Primitives.wrap(type);
        if (primitive == PersistentDataContainer.class) {
            requireValue(primitive, value);
            return compound((PersistentDataContainer) value);
        }
        if (primitive == PersistentDataContainer[].class) {
            requireValue(primitive, value);
            NbtList list = new NbtList();
            for (PersistentDataContainer container : (PersistentDataContainer[]) value) {
                list.add(compound(container));
            }
            return list;
        }
        tagType(primitive);
        requireValue(primitive, value);
        return NativeItemMetadata.encode(Map.of("value", value)).get("value");
    }

    public <T> boolean isInstanceOf(Class<T> type, NbtElement tag) {
        return tag != null && tag.getType() == tagType(Primitives.wrap(type));
    }

    public <T> T extract(Class<T> type, NbtElement tag) {
        Class<T> primitive = Primitives.wrap(type);
        if (!isInstanceOf(primitive, tag)) {
            throw new IllegalArgumentException("The tag cannot store " + primitive.getSimpleName());
        }
        Object value;
        if (primitive == PersistentDataContainer.class) {
            CraftPersistentDataContainer container = new CraftPersistentDataContainer(this);
            container.putAll((NbtCompound) tag);
            value = container;
        } else if (primitive == PersistentDataContainer[].class) {
            NbtList list = (NbtList) tag;
            CraftPersistentDataContainer[] containers = new CraftPersistentDataContainer[list.size()];
            for (int index = 0; index < containers.length; index++) {
                containers[index] = new CraftPersistentDataContainer(this);
                containers[index].putAll(list.getCompound(index));
            }
            value = containers;
        } else {
            NbtCompound compound = new NbtCompound();
            compound.put("value", tag);
            value = NativeItemMetadata.decode(compound).get("value");
        }
        return primitive.cast(value);
    }

    private static void requireValue(Class<?> primitive, Object value) {
        if (!primitive.isInstance(value)) throw new IllegalArgumentException("Expected primitive " + primitive.getSimpleName());
    }

    private static NbtCompound compound(PersistentDataContainer container) {
        if (container instanceof CraftPersistentDataContainer own) return own.toTagCompound();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(container.serializeToBytes()))) {
            return NbtIo.read(input);
        } catch (IOException failure) {
            throw new UncheckedIOException("Cannot read persistent container NBT", failure);
        }
    }

    private static byte tagType(Class<?> primitive) {
        if (primitive == Byte.class) return NbtElement.BYTE_TYPE;
        if (primitive == Short.class) return NbtElement.SHORT_TYPE;
        if (primitive == Integer.class) return NbtElement.INT_TYPE;
        if (primitive == Long.class) return NbtElement.LONG_TYPE;
        if (primitive == Float.class) return NbtElement.FLOAT_TYPE;
        if (primitive == Double.class) return NbtElement.DOUBLE_TYPE;
        if (primitive == String.class) return NbtElement.STRING_TYPE;
        if (primitive == byte[].class) return NbtElement.BYTE_ARRAY_TYPE;
        if (primitive == int[].class) return NbtElement.INT_ARRAY_TYPE;
        if (primitive == long[].class) return NbtElement.LONG_ARRAY_TYPE;
        if (primitive == PersistentDataContainer.class) return NbtElement.COMPOUND_TYPE;
        if (primitive == PersistentDataContainer[].class) return NbtElement.LIST_TYPE;
        throw new IllegalArgumentException("No native tag adapter for " + primitive.getSimpleName());
    }
}
