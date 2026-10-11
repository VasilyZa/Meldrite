package org.bukkit.craftbukkit.v1_20_R1.util;

import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.bukkit.ChatColor;

/** Legacy Bukkit chat conversion for the 1.20.1 API. */
public final class CraftChatMessage {
    private CraftChatMessage() {}

    public static Text fromStringOrNull(String message) { return fromStringOrNull(message, false); }
    public static Text fromStringOrNull(String message, boolean keepNewlines) {
        return message == null || message.isEmpty() ? null : fromString(message, keepNewlines)[0];
    }
    public static Text[] fromString(String message) { return fromString(message, false); }
    public static Text[] fromString(String message, boolean keepNewlines) {
        return new StringMessage(message, keepNewlines, false).getOutput();
    }
    private static Text[] parseLegacy(String message, boolean keepNewlines, boolean plain) {
        if (plain) return new Text[] {Text.literal(ChatColor.stripColor(message == null ? "" : message))};
        if (message == null) return new Text[] {Text.empty()};
        List<Text> result = new ArrayList<>();
        MutableText current = Text.empty();
        Style style = Style.EMPTY;
        StringBuilder part = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            char character = message.charAt(i);
            if (character == ChatColor.COLOR_CHAR && i + 1 < message.length()) {
                if (part.length() > 0) { current.append(Text.literal(part.toString()).setStyle(style)); part.setLength(0); }
                Formatting formatting = Formatting.byCode(Character.toLowerCase(message.charAt(++i)));
                if (formatting == Formatting.RESET) style = Style.EMPTY;
                else if (formatting != null) style = formatting.isModifier() ? style.withFormatting(formatting) : Style.EMPTY.withColor(formatting);
                continue;
            }
            if (character == '\n' && !keepNewlines) {
                if (part.length() > 0) { current.append(Text.literal(part.toString()).setStyle(style)); part.setLength(0); }
                result.add(current);
                current = Text.empty();
            } else {
                part.append(character);
            }
        }
        if (part.length() > 0) current.append(Text.literal(part.toString()).setStyle(style));
        result.add(current);
        return result.toArray(Text[]::new);
    }
    public static Text[] fromString(String message, boolean keepNewlines, boolean plain) {
        return new StringMessage(message, keepNewlines, plain).getOutput();
    }
    public static String toJSON(Text component) { return Text.Serializer.toJson(component); }
    public static String toJSONOrNull(Text component) { return component == null ? null : toJSON(component); }
    public static Text fromJSON(String json) throws JsonParseException { return Text.Serializer.fromJson(json); }
    public static Text fromJSONOrNull(String json) {
        if (json == null) return null;
        try { return fromJSON(json); } catch (JsonParseException ignored) { return null; }
    }
    public static Text fromJSONOrString(String message) { return fromJSONOrString(message, false); }
    public static Text fromJSONOrString(String message, boolean keepNewlines) {
        Text parsed = fromJSONOrNull(message);
        return parsed != null ? parsed : fromString(message, keepNewlines)[0];
    }
    public static String fromStringToJSON(String message) { return fromStringToJSON(message, false); }
    public static String fromStringToJSON(String message, boolean keepNewlines) { return toJSON(fromString(message, keepNewlines)[0]); }
    public static String fromStringOrNullToJSON(String message) { return toJSONOrNull(fromStringOrNull(message)); }
    public static String fromJSONOrStringToJSON(String message) { return fromJSONOrStringToJSON(message, false); }
    public static String fromJSONOrStringToJSON(String message, boolean keepNewlines) { return toJSON(fromJSONOrString(message, keepNewlines)); }
    public static String fromJSONOrStringOrNullToJSON(String message) { return message == null || message.isEmpty() ? null : fromJSONOrStringToJSON(message); }
    public static String fromJSONComponent(String json) { Text component = fromJSONOrNull(json); return component == null ? "" : component.getString(); }
    public static String fromComponent(Text component) {
        if (component == null) return "";
        StringBuilder result = new StringBuilder();
        Formatting[] lastColor = new Formatting[1];
        boolean[] modifiers = new boolean[5];
        component.visit((style, content) -> {
            if (content.isEmpty()) return Optional.empty();
            TextColor color = style.getColor();
            Formatting nextColor = color == null ? null : Formatting.byName(color.getName());
            if (nextColor != null && nextColor != lastColor[0]) {
                result.append(ChatColor.COLOR_CHAR).append(nextColor.getCode());
                lastColor[0] = nextColor;
                java.util.Arrays.fill(modifiers, false);
            }
            boolean[] next = {style.isBold(), style.isItalic(), style.isUnderlined(),
                    style.isStrikethrough(), style.isObfuscated()};
            char[] codes = {'l', 'o', 'n', 'm', 'k'};
            for (int i = 0; i < next.length; i++) {
                if (next[i] && !modifiers[i]) result.append(ChatColor.COLOR_CHAR).append(codes[i]);
                modifiers[i] = next[i];
            }
            result.append(content);
            return Optional.empty();
        }, Style.EMPTY);
        return result.toString();
    }
    public static Formatting getColor(ChatColor color) { return color == null ? null : Formatting.byCode(color.getChar()); }
    public static ChatColor getColor(Formatting formatting) { return formatting == null ? null : ChatColor.getByChar(formatting.getCode()); }

    /** Internal parser holder retained for CraftBukkit reflective callers. */
    private static final class StringMessage {
        private final Text[] output;
        private StringMessage(String message, boolean keepNewlines, boolean plain) {
            this.output = parseLegacy(message, keepNewlines, plain);
        }
        private Text[] getOutput() { return output; }
    }
}
