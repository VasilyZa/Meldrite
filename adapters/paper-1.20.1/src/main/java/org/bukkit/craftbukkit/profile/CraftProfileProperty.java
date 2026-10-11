package org.bukkit.craftbukkit.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Authlib property conversion helpers used by CraftBukkit profile code. */
public final class CraftProfileProperty {
    public interface JsonFormatter {
        JsonFormatter COMPACT = new JsonFormatter() {
            private final Gson gson = new GsonBuilder().create();
            @Override public String format(JsonElement element) { return gson.toJson(element); }
        };
        String format(JsonElement element);
    }

    private CraftProfileProperty() { }

    public static boolean hasValidSignature(Property property) {
        return property != null && property.hasSignature() && dev.meldrite.paper.facade.PlayerProfileAdapter.isSigned(property);
    }

    public static JsonObject decodePropertyValue(String encoded) {
        if (encoded == null) return null;
        try {
            String json = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            JsonElement element = JsonParser.parseString(json);
            return element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (IllegalArgumentException | JsonParseException failure) {
            return null;
        }
    }

    public static String encodePropertyValue(JsonObject value, JsonFormatter formatter) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(formatter, "formatter");
        return Base64.getEncoder().encodeToString(formatter.format(value).getBytes(StandardCharsets.UTF_8));
    }

    public static String toString(Property property) {
        return "{name=" + property.getName() + ", value=" + property.getValue() + ", signature=" + property.getSignature() + "}";
    }

    public static int hashCode(Property property) {
        return Objects.hash(property.getName(), property.getValue(), property.getSignature());
    }

    public static boolean equals(Property first, Property second) {
        if (first == second) return true;
        return first != null && second != null && Objects.equals(first.getName(), second.getName())
                && Objects.equals(first.getValue(), second.getValue()) && Objects.equals(first.getSignature(), second.getSignature());
    }

    public static Map<String, Object> serialize(Property property) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", property.getName());
        result.put("value", property.getValue());
        if (property.hasSignature()) result.put("signature", property.getSignature());
        return result;
    }

    public static Property deserialize(Map<?, ?> values) {
        Object name = values.get("name");
        Object value = values.get("value");
        if (!(name instanceof String) || !(value instanceof String)) throw new IllegalArgumentException("profile property requires name and value");
        Object signature = values.get("signature");
        return new Property((String) name, (String) value, signature == null ? null : String.valueOf(signature));
    }
}
