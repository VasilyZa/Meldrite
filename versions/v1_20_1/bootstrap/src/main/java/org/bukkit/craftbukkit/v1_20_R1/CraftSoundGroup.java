package org.bukkit.craftbukkit.v1_20_R1;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;

/** A sound group keeps the native implementation, including custom volume and pitch. */
public final class CraftSoundGroup implements SoundGroup {
    private static final Map<Identifier, Sound> SOUNDS = sounds();
    private final BlockSoundGroup handle;

    public CraftSoundGroup(BlockSoundGroup handle) { this.handle = java.util.Objects.requireNonNull(handle); }
    public static CraftSoundGroup getSoundGroup(BlockSoundGroup handle) { return new CraftSoundGroup(handle); }
    public BlockSoundGroup getHandle() { return handle; }
    @Override public float getVolume() { return handle.getVolume(); }
    @Override public float getPitch() { return handle.getPitch(); }
    @Override public Sound getBreakSound() { return sound(handle.getBreakSound()); }
    @Override public Sound getStepSound() { return sound(handle.getStepSound()); }
    @Override public Sound getPlaceSound() { return sound(handle.getPlaceSound()); }
    @Override public Sound getHitSound() { return sound(handle.getHitSound()); }
    @Override public Sound getFallSound() { return sound(handle.getFallSound()); }

    private static Sound sound(SoundEvent event) {
        Sound sound = SOUNDS.get(event.getId());
        if (sound == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_SOUND: " + event.getId());
        return sound;
    }
    private static Map<Identifier, Sound> sounds() {
        var sounds = new HashMap<Identifier, Sound>();
        for (Sound sound : Sound.values()) sounds.put(new Identifier(sound.getKey().getNamespace(), sound.getKey().getKey()), sound);
        return Map.copyOf(sounds);
    }
}
