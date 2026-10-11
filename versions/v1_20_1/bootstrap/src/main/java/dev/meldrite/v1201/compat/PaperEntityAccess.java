package dev.meldrite.v1201.compat;

import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import net.kyori.adventure.util.TriState;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

/** Craft entity state stored on each native entity, including plugin-defined subclasses. */
public interface PaperEntityAccess {
    /** Bukkit session state follows a native replacement, independently of vanilla saved data. */
    record SessionState(boolean visibleByDefault, boolean persistent, CraftPersistentDataContainer persistentData,
            int maximumNoDamageTicks, org.bukkit.event.entity.EntityDamageEvent lastDamageCause,
            boolean lastDamageCancelled, boolean maxAirOverridden, int maxAir, boolean collidable,
            boolean collidableOverridden, Set<UUID> collidableExemptions, TriState friction, int shieldBlockingDelay,
            boolean fixedPose, boolean visualFire, java.util.Map<String, java.util.IdentityHashMap<Plugin, MetadataValue>> metadata) { }
    SessionState meldrite$exportSessionState();
    void meldrite$importSessionState(SessionState state);
    boolean meldrite$isVisibleByDefault();
    void meldrite$setVisibleByDefault(boolean visible);
    boolean meldrite$isPersistent();
    void meldrite$setPersistent(boolean persistent);
    boolean meldrite$isTracked();
    void meldrite$setTracked(boolean tracked);
    CraftPersistentDataContainer meldrite$getPersistentDataContainer();
    CraftEntity meldrite$getCachedBukkitEntity();
    void meldrite$setCachedBukkitEntity(CraftEntity entity);
    int meldrite$getMaximumNoDamageTicks();
    void meldrite$setMaximumNoDamageTicks(int ticks);
    org.bukkit.event.entity.EntityDamageEvent meldrite$getLastDamageCause();
    void meldrite$setLastDamageCause(org.bukkit.event.entity.EntityDamageEvent event);
    void meldrite$setLastDamageCancelled(boolean cancelled);
    boolean meldrite$hasMaxAirOverride();
    int meldrite$getMaxAirOverride();
    void meldrite$setMaxAirOverride(int ticks);
    boolean meldrite$isCollidable();
    void meldrite$setCollidable(boolean collidable);
    Set<UUID> meldrite$getCollidableExemptions();
    TriState meldrite$getFrictionState();
    void meldrite$setFrictionState(TriState state);
    int meldrite$getShieldBlockingDelay();
    void meldrite$setShieldBlockingDelay(int delay);
    boolean meldrite$isFixedPose();
    void meldrite$setFixedPose(boolean fixed);
    boolean meldrite$hasVisualFire();
    void meldrite$setVisualFire(boolean visualFire);
    void meldrite$setMetadata(String key, MetadataValue value);
    List<MetadataValue> meldrite$getMetadata(String key);
    boolean meldrite$hasMetadata(String key);
    void meldrite$removeMetadata(String key, Plugin plugin);
}
