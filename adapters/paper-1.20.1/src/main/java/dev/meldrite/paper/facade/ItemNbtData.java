package dev.meldrite.paper.facade;

import com.google.gson.Gson;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Detached Java representation of an item tag, including tags owned by mods. */
public final class ItemNbtData {
    private static final Gson JSON = new Gson();
    private ItemNbtData() { }

    public static Map<String, Object> copy(Map<String, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(key, copyValue(value)));
        return result;
    }

    public static Object copyValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((key, item) -> copy.put((String) key, copyValue(item)));
            return copy;
        }
        if (value instanceof List<?> list) return new ArrayList<>(list.stream().map(ItemNbtData::copyValue).toList());
        if (value instanceof byte[] array) return array.clone();
        if (value instanceof int[] array) return array.clone();
        if (value instanceof long[] array) return array.clone();
        if (value instanceof String || value instanceof Number || value instanceof UUID || value instanceof Boolean) return value;
        throw new IllegalArgumentException("NOT_NBT_VALUE: " + value);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> compound(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
    }

    public static String snbt(Object value) {
        if (value instanceof Map<?, ?> map) {
            StringJoiner result = new StringJoiner(",", "{", "}");
            map.keySet().stream().map(String.class::cast).sorted().forEach(key -> result.add(JSON.toJson(key) + ":" + snbt(map.get(key))));
            return result.toString();
        }
        if (value instanceof List<?> list) return list.stream().map(ItemNbtData::snbt).collect(java.util.stream.Collectors.joining(",", "[", "]"));
        if (value instanceof UUID uuid) return snbt(uuidArray(uuid));
        if (value instanceof byte[] array) {
            StringJoiner result = new StringJoiner(",", "[B;", "]");
            for (byte item : array) result.add(item + "b");
            return result.toString();
        }
        if (value instanceof int[] array) {
            StringJoiner result = new StringJoiner(",", "[I;", "]");
            for (int item : array) result.add(Integer.toString(item));
            return result.toString();
        }
        if (value instanceof long[] array) {
            StringJoiner result = new StringJoiner(",", "[L;", "]");
            for (long item : array) result.add(item + "L");
            return result.toString();
        }
        if (value instanceof Byte) return value + "b";
        if (value instanceof Short) return value + "s";
        if (value instanceof Long) return value + "L";
        if (value instanceof Float) return value + "f";
        if (value instanceof Double) return value + "d";
        if (value instanceof Boolean flag) return flag ? "1b" : "0b";
        return JSON.toJson(value);
    }

    public static int[] uuidArray(UUID uuid) {
        return new int[]{(int) (uuid.getMostSignificantBits() >>> 32), (int) uuid.getMostSignificantBits(),
                (int) (uuid.getLeastSignificantBits() >>> 32), (int) uuid.getLeastSignificantBits()};
    }

    public static UUID uuid(Object value) {
        if (value instanceof UUID uuid) return uuid;
        if (value instanceof int[] array && array.length == 4) {
            return new UUID((long) array[0] << 32 | array[1] & 0xffffffffL,
                    (long) array[2] << 32 | array[3] & 0xffffffffL);
        }
        if (value instanceof String text) {
            try { return UUID.fromString(text); } catch (IllegalArgumentException ignored) { }
        }
        return null;
    }

    public static byte[] encode(Map<String, Object> compound) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(new GZIPOutputStream(result))) {
            output.writeByte(10);
            output.writeUTF("");
            write(output, 10, compound);
        }
        return result.toByteArray();
    }

    public static Map<String, Object> decode(byte[] bytes) throws IOException {
        try (DataInputStream input = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)))) {
            if (input.readUnsignedByte() != 10) throw new IOException("NBT_ROOT_NOT_COMPOUND");
            input.readUTF();
            return compound(read(input, 10));
        }
    }

    public static byte[] encodeUncompressed(Map<String, Object> compound) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(result)) {
            output.writeByte(10);
            output.writeUTF("");
            write(output, 10, compound);
        }
        return result.toByteArray();
    }

    public static Map<String, Object> decodeUncompressed(byte[] bytes) throws IOException {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (input.readUnsignedByte() != 10) throw new IOException("NBT_ROOT_NOT_COMPOUND");
            input.readUTF();
            return compound(read(input, 10));
        }
    }

    private static int type(Object value) {
        if (value instanceof Byte || value instanceof Boolean) return 1;
        if (value instanceof Short) return 2;
        if (value instanceof Integer) return 3;
        if (value instanceof Long) return 4;
        if (value instanceof Float) return 5;
        if (value instanceof Double) return 6;
        if (value instanceof byte[]) return 7;
        if (value instanceof String) return 8;
        if (value instanceof List<?>) return 9;
        if (value instanceof Map<?, ?>) return 10;
        if (value instanceof int[] || value instanceof UUID) return 11;
        if (value instanceof long[]) return 12;
        throw new IllegalArgumentException("NOT_NBT_VALUE: " + value);
    }

    private static void write(DataOutputStream output, int type, Object value) throws IOException {
        switch (type) {
            case 1 -> output.writeByte(value instanceof Boolean flag ? flag ? 1 : 0 : ((Number) value).byteValue());
            case 2 -> output.writeShort(((Number) value).shortValue());
            case 3 -> output.writeInt(((Number) value).intValue());
            case 4 -> output.writeLong(((Number) value).longValue());
            case 5 -> output.writeFloat(((Number) value).floatValue());
            case 6 -> output.writeDouble(((Number) value).doubleValue());
            case 7 -> { byte[] array = (byte[]) value; output.writeInt(array.length); output.write(array); }
            case 8 -> output.writeUTF((String) value);
            case 9 -> {
                List<?> list = (List<?>) value;
                int itemType = list.isEmpty() ? 0 : type(list.get(0));
                output.writeByte(itemType); output.writeInt(list.size());
                for (Object item : list) {
                    if (type(item) != itemType) throw new IOException("HETEROGENEOUS_NBT_LIST");
                    write(output, itemType, item);
                }
            }
            case 10 -> {
                for (var entry : compound(value).entrySet()) {
                    int itemType = type(entry.getValue());
                    output.writeByte(itemType); output.writeUTF(entry.getKey()); write(output, itemType, entry.getValue());
                }
                output.writeByte(0);
            }
            case 11 -> { int[] array = value instanceof UUID uuid ? uuidArray(uuid) : (int[]) value; output.writeInt(array.length); for (int item : array) output.writeInt(item); }
            case 12 -> { long[] array = (long[]) value; output.writeInt(array.length); for (long item : array) output.writeLong(item); }
            default -> throw new IOException("INVALID_NBT_TYPE: " + type);
        }
    }

    private static Object read(DataInputStream input, int type) throws IOException {
        return switch (type) {
            case 1 -> input.readByte();
            case 2 -> input.readShort();
            case 3 -> input.readInt();
            case 4 -> input.readLong();
            case 5 -> input.readFloat();
            case 6 -> input.readDouble();
            case 7 -> { byte[] array = new byte[length(input)]; input.readFully(array); yield array; }
            case 8 -> input.readUTF();
            case 9 -> { int itemType = input.readUnsignedByte(); int size = length(input); List<Object> list = new ArrayList<>(size); for (int i = 0; i < size; i++) list.add(read(input, itemType)); yield list; }
            case 10 -> { Map<String, Object> map = new LinkedHashMap<>(); int itemType; while ((itemType = input.readUnsignedByte()) != 0) map.put(input.readUTF(), read(input, itemType)); yield map; }
            case 11 -> { int[] array = new int[length(input)]; for (int i = 0; i < array.length; i++) array[i] = input.readInt(); yield array; }
            case 12 -> { long[] array = new long[length(input)]; for (int i = 0; i < array.length; i++) array[i] = input.readLong(); yield array; }
            default -> throw new IOException("INVALID_NBT_TYPE: " + type);
        };
    }

    private static int length(DataInputStream input) throws IOException {
        int result = input.readInt();
        if (result < 0 || result > 16 * 1024 * 1024) throw new IOException("INVALID_NBT_LENGTH: " + result);
        return result;
    }
}
