package org.bukkit.craftbukkit.util;

import com.google.gson.JsonParseException;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.bukkit.ChatColor;

/** Legacy package entry point retained for plugins compiled against CraftBukkit. */
public final class CraftChatMessage {
    private CraftChatMessage() {}

    public static Text fromStringOrNull(String message) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromStringOrNull(message); }
    public static Text fromStringOrNull(String message, boolean keepNewlines) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromStringOrNull(message, keepNewlines);
    }
    public static Text[] fromString(String message) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromString(message); }
    public static Text[] fromString(String message, boolean keepNewlines) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromString(message, keepNewlines);
    }
    public static Text[] fromString(String message, boolean keepNewlines, boolean plain) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromString(message, keepNewlines, plain);
    }
    public static String toJSON(Text component) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.toJSON(component); }
    public static String toJSONOrNull(Text component) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.toJSONOrNull(component); }
    public static Text fromJSON(String json) throws JsonParseException { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromJSON(json); }
    public static Text fromJSONOrNull(String json) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromJSONOrNull(json); }
    public static Text fromJSONOrString(String message) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromJSONOrString(message); }
    public static Text fromJSONOrString(String message, boolean keepNewlines) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromJSONOrString(message, keepNewlines);
    }
    public static String fromStringToJSON(String message) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromStringToJSON(message); }
    public static String fromStringToJSON(String message, boolean keepNewlines) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromStringToJSON(message, keepNewlines);
    }
    public static String fromStringOrNullToJSON(String message) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromStringOrNullToJSON(message);
    }
    public static String fromJSONOrStringToJSON(String message) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromJSONOrStringToJSON(message);
    }
    public static String fromJSONOrStringToJSON(String message, boolean keepNewlines) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromJSONOrStringToJSON(message, keepNewlines);
    }
    public static String fromJSONOrStringOrNullToJSON(String message) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromJSONOrStringOrNullToJSON(message);
    }
    public static String fromJSONComponent(String json) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromJSONComponent(json); }
    public static String fromComponent(Text component) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromComponent(component); }
    public static Formatting getColor(ChatColor color) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.getColor(color); }
    public static ChatColor getColor(Formatting formatting) { return org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.getColor(formatting); }
}
