package org.bukkit.craftbukkit.v1_20_R1;

import net.minecraft.item.Instrument;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;

public final class CraftMusicInstrument extends MusicInstrument {
    private final NamespacedKey key;
    private final Instrument handle;
    public CraftMusicInstrument(NamespacedKey key, Instrument handle) {
        this.key = java.util.Objects.requireNonNull(key); this.handle = java.util.Objects.requireNonNull(handle);
    }
    @Override public NamespacedKey getKey() { return key; }
    public Instrument getHandle() { return handle; }
    @Override public boolean equals(Object other) { return other instanceof CraftMusicInstrument value && key.equals(value.key); }
    @Override public int hashCode() { return key.hashCode(); }
    @Override public String toString() { return "CraftMusicInstrument{key=" + key + "}"; }
}
