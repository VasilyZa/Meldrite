package dev.meldrite.v1201.compat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Player-specific Bukkit state retained by the native player throughout its session. */
public final class NativePlayerState {
    public volatile String locale = "en_us";
    public volatile int clientViewDistance = -1;
    public volatile int skinParts;
    public volatile String clientBrand;
    /** Retains the Bukkit property; the 1.20.1 player-info protocol has no list-order field. */
    public volatile int playerListOrder;
    public long timeOffset;
    public boolean relativeTime = true;
    public org.bukkit.WeatherType weather;
    public boolean sleepingIgnored;
    public org.bukkit.Location compassTarget;
    public int viewDistance = -1;
    public int simulationDistance = -1;
    public long firstPlayed = System.currentTimeMillis();
    public long lastPlayed = firstPlayed;
    public long lastLogin = firstPlayed;
    public long lastSeen = firstPlayed;
    public boolean playedBefore;
    public boolean kickLeaveMessageSet;
    public net.kyori.adventure.text.Component kickLeaveMessage;
    public final Set<String> channels = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final Map<UUID, List<WeakReference<Object>>> invertedVisibility = new HashMap<>();

    public long playerTime(long worldTime) {
        return (relativeTime ? worldTime : worldTime - worldTime % 24000) + timeOffset;
    }
    public synchronized boolean canSee(UUID entity, boolean visibleByDefault) {
        return visibleByDefault ^ invertedVisibility.containsKey(entity);
    }
    public synchronized void resetVisibility(UUID entity) { invertedVisibility.remove(entity); }
    /** Returns whether the client's effective visibility changed. Owners are compared by identity. */
    public synchronized boolean visibility(UUID entity, boolean visibleByDefault, Object owner, boolean visible) {
        boolean before = canSee(entity, visibleByDefault);
        boolean invert = visible != visibleByDefault;
        var owners = invertedVisibility.get(entity);
        if (invert) {
            if (owners == null) { owners = new ArrayList<>(); invertedVisibility.put(entity, owners); }
            if (owners.stream().noneMatch(reference -> reference == null ? owner == null : reference.get() == owner)) {
                owners.add(owner == null ? null : new WeakReference<>(owner));
            }
        } else if (owners != null) {
            owners.removeIf(reference -> reference == null ? owner == null : reference.get() == owner || reference.get() == null);
            if (owners.isEmpty()) invertedVisibility.remove(entity);
        }
        return before != canSee(entity, visibleByDefault);
    }
}
