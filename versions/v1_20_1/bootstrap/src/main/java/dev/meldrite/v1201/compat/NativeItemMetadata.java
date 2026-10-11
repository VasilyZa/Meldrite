package dev.meldrite.v1201.compat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.*;

/** Lossless 1.20.1 item-tag representation across the profile/native boundary. */
public final class NativeItemMetadata {
    private NativeItemMetadata() {}

    public static NbtCompound encode(Map<String, Object> values) {
        NbtCompound tag = new NbtCompound();
        values.forEach((key, value) -> tag.put(key, encodeValue(value)));
        return tag;
    }

    private static NbtElement encodeValue(Object value) {
        if (value instanceof Byte number) return NbtByte.of(number);
        if (value instanceof Short number) return NbtShort.of(number);
        if (value instanceof Integer number) return NbtInt.of(number);
        if (value instanceof Long number) return NbtLong.of(number);
        if (value instanceof Float number) return NbtFloat.of(number);
        if (value instanceof Double number) return NbtDouble.of(number);
        if (value instanceof Boolean flag) return NbtByte.of(flag);
        if (value instanceof String text) return NbtString.of(text);
        if (value instanceof UUID id) return NbtHelper.fromUuid(id);
        if (value instanceof byte[] array) return new NbtByteArray(array.clone());
        if (value instanceof int[] array) return new NbtIntArray(array.clone());
        if (value instanceof long[] array) return new NbtLongArray(array.clone());
        if (value instanceof Map<?, ?> map) {
            NbtCompound compound = new NbtCompound();
            map.forEach((key, entry) -> compound.put(String.class.cast(key), encodeValue(entry)));
            return compound;
        }
        if (value instanceof List<?> list) {
            NbtList entries = new NbtList();
            for (Object entry : list) {
                if (!entries.add(encodeValue(entry))) throw new IllegalArgumentException("MIXED_NBT_LIST_TYPES");
            }
            return entries;
        }
        throw new IllegalArgumentException("UNSUPPORTED_NBT_VALUE: " + (value == null ? "null" : value.getClass()));
    }

    public static Map<String, Object> decode(NbtCompound tag) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (tag != null) for (String key : tag.getKeys()) result.put(key, decodeValue(tag.get(key)));
        return result;
    }

    private static Object decodeValue(NbtElement tag) {
        return switch (tag.getType()) {
            case NbtElement.BYTE_TYPE -> ((AbstractNbtNumber) tag).byteValue();
            case NbtElement.SHORT_TYPE -> ((AbstractNbtNumber) tag).shortValue();
            case NbtElement.INT_TYPE -> ((AbstractNbtNumber) tag).intValue();
            case NbtElement.LONG_TYPE -> ((AbstractNbtNumber) tag).longValue();
            case NbtElement.FLOAT_TYPE -> ((AbstractNbtNumber) tag).floatValue();
            case NbtElement.DOUBLE_TYPE -> ((AbstractNbtNumber) tag).doubleValue();
            case NbtElement.STRING_TYPE -> tag.asString();
            case NbtElement.BYTE_ARRAY_TYPE -> ((NbtByteArray) tag).getByteArray().clone();
            case NbtElement.INT_ARRAY_TYPE -> ((NbtIntArray) tag).getIntArray().clone();
            case NbtElement.LONG_ARRAY_TYPE -> ((NbtLongArray) tag).getLongArray().clone();
            case NbtElement.COMPOUND_TYPE -> decode((NbtCompound) tag);
            case NbtElement.LIST_TYPE -> {
                List<Object> values = new ArrayList<>();
                for (NbtElement element : (NbtList) tag) values.add(decodeValue(element));
                yield values;
            }
            default -> throw new IllegalArgumentException("UNSUPPORTED_NBT_TYPE: " + tag.getType());
        };
    }
}
