package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.text.Text;

final class ScoreboardText {
    private static final GsonComponentSerializer JSON = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private ScoreboardText() {}
    static Text nativeText(Component value) { return Text.Serializer.fromJson(JSON.serialize(value == null ? Component.empty() : value)); }
    static Component component(Text value) { return JSON.deserialize(Text.Serializer.toJson(value)); }
    static Text legacy(String value) {
        if (value == null) throw new IllegalArgumentException("Text cannot be null");
        return nativeText(LEGACY.deserialize(value));
    }
    static String legacy(Text value) { return LEGACY.serialize(component(value)); }
}
