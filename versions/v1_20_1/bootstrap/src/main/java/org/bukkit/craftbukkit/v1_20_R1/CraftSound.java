package org.bukkit.craftbukkit.v1_20_R1;

import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import org.bukkit.Sound;

public final class CraftSound {
    private CraftSound() {}
    public static SoundEvent getSoundEffect(String name) {
        if (name == null) throw new IllegalArgumentException("sound");
        var key = org.bukkit.NamespacedKey.fromString(name);
        if (key == null) throw new IllegalArgumentException("Invalid sound: " + name);
        var event = Registries.SOUND_EVENT.get(new net.minecraft.util.Identifier(key.getNamespace(), key.getKey()));
        if (event == null) throw new IllegalArgumentException("Sound does not exist: " + name);
        return event;
    }
    public static SoundEvent getSoundEffect(Sound sound) {
        return getSoundEffect(sound.getKey().toString());
    }
    public static Sound getBukkit(SoundEvent event) {
        if (event == null) return null;
        var key = Registries.SOUND_EVENT.getId(event);
        return org.bukkit.Registry.SOUNDS.get(org.bukkit.NamespacedKey.fromString(key.toString()));
    }
    public static String getSound(Sound sound) { return sound.getKey().getKey(); }
}
