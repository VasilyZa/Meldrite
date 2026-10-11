package org.bukkit.craftbukkit.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;

/** Conversion used by Bukkit's YAML layer for Mojangson-compatible NBT values. */
public final class CraftNBTTagConfigSerializer {
    private static final Pattern INTEGER = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)i", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOUBLE = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", Pattern.CASE_INSENSITIVE);

    private CraftNBTTagConfigSerializer() {}

    public static Object serialize(NbtElement base) {
        if (base instanceof NbtCompound compound) {
            Map<String, Object> result = new HashMap<>();
            for (String key : compound.getKeys()) result.put(key, serialize(compound.get(key)));
            return result;
        }
        if (base instanceof NbtList list) {
            List<Object> result = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) result.add(serialize(list.get(i)));
            return result;
        }
        if (base instanceof NbtString) return base.asString();
        if (base instanceof NbtInt) return base.toString() + "i";
        return base.toString();
    }

    @SuppressWarnings("unchecked")
    public static NbtElement deserialize(Object object) {
        if (object instanceof Map<?, ?> map) {
            NbtCompound result = new NbtCompound();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key)) throw new IllegalArgumentException("NBT map keys must be strings");
                result.put(key, deserialize(entry.getValue()));
            }
            return result;
        }
        if (object instanceof List<?> list) {
            NbtList result = new NbtList();
            for (Object value : list) result.add(deserialize(value));
            return result;
        }
        if (object instanceof String string) {
            if (INTEGER.matcher(string).matches()) return NbtInt.of(Integer.parseInt(string.substring(0, string.length() - 1)));
            if (DOUBLE.matcher(string).matches()) return NbtDouble.of(Double.parseDouble(string.substring(0, string.length() - 1)));
            try {
                NbtElement parsed = new StringNbtReader(new StringReader(string)).parseElement();
                if (parsed instanceof NbtInt) return NbtString.of(parsed.asString());
                if (parsed instanceof NbtDouble number) return NbtString.of(Double.toString(number.doubleValue()));
                return parsed;
            } catch (CommandSyntaxException ignored) {
                return NbtString.of(string);
            }
        }
        throw new IllegalArgumentException("Could not deserialize NBT value: " + object);
    }
}
