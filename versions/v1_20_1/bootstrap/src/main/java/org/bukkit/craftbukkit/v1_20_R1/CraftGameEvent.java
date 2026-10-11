package org.bukkit.craftbukkit.v1_20_R1;

import net.minecraft.world.event.GameEvent;
import org.bukkit.NamespacedKey;

public final class CraftGameEvent extends org.bukkit.GameEvent {
    private final NamespacedKey key;
    private final GameEvent handle;
    public CraftGameEvent(NamespacedKey key, GameEvent handle) {
        this.key = java.util.Objects.requireNonNull(key); this.handle = java.util.Objects.requireNonNull(handle);
    }
    @Override public NamespacedKey getKey() { return key; }
    public GameEvent getHandle() { return handle; }
    @Override public boolean equals(Object other) { return other instanceof CraftGameEvent value && key.equals(value.key); }
    @Override public int hashCode() { return key.hashCode(); }
    @Override public String toString() { return "CraftGameEvent{key=" + key + "}"; }
}
