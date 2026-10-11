package org.bukkit.craftbukkit.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public final class JsonHelper {
    private JsonHelper() {}

    public static JsonObject getObjectOrNull(JsonObject parent, String key) {
        JsonElement element = parent.get(key);
        return element instanceof JsonObject object ? object : null;
    }

    public static JsonObject getOrCreateObject(JsonObject parent, String key) {
        JsonObject object = getObjectOrNull(parent, key);
        if (object == null) {
            object = new JsonObject();
            parent.add(key, object);
        }
        return object;
    }

    public static JsonPrimitive getPrimitiveOrNull(JsonObject parent, String key) {
        JsonElement element = parent.get(key);
        return element instanceof JsonPrimitive primitive ? primitive : null;
    }

    public static String getStringOrNull(JsonObject parent, String key) {
        JsonPrimitive primitive = getPrimitiveOrNull(parent, key);
        return primitive == null ? null : primitive.getAsString();
    }

    public static void setOrRemove(JsonObject parent, String key, JsonElement value) {
        if (value == null) parent.remove(key);
        else parent.add(key, value);
    }
}
