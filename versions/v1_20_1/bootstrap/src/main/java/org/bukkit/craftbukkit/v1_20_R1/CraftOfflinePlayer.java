package org.bukkit.craftbukkit.v1_20_R1;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.WorldSavePath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.v1_20_R1.ban.CraftProfileBanEntry;

@org.bukkit.configuration.serialization.SerializableAs("Player")
public class CraftOfflinePlayer implements OfflinePlayer {
    private final CraftServer server;
    private final GameProfile profile;
    protected CraftOfflinePlayer(CraftServer server, GameProfile profile) {
        this.server = java.util.Objects.requireNonNull(server); this.profile = java.util.Objects.requireNonNull(profile);
    }
    public CraftServer getServer() { return server; }
    @Override public UUID getUniqueId() { return profile.getId(); }
    @Override public Player getPlayer() { return server.getPlayer(getUniqueId()); }
    @Override public boolean isOnline() { return getPlayer() != null; }
    @Override public boolean isConnected() { var player = getPlayer(); return player != null && player.isConnected(); }
    private Path dataFile() { return server.getServer().getSavePath(WorldSavePath.PLAYERDATA).resolve(getUniqueId() + ".dat"); }
    private NbtCompound data() {
        var path = dataFile(); if (!Files.isRegularFile(path)) return null;
        try { return NbtIo.readCompressed(path.toFile()); }
        catch (IOException failure) { throw new IllegalStateException("Cannot read player data: " + path, failure); }
    }
    @Override public String getName() {
        var player = getPlayer(); if (player != null) return player.getName();
        if (profile.getName() != null) return profile.getName();
        var data = data(); return data == null || !data.getCompound("bukkit").contains("lastKnownName") ? null : data.getCompound("bukkit").getString("lastKnownName");
    }
    @Override public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        var value = new dev.meldrite.paper.facade.PlayerProfileAdapter(getUniqueId(), getName());
        value.completeFromCache(false, server.getOnlineMode()); return value;
    }
    private void writable() { if (!server.getServer().isOnThread()) throw new IllegalStateException("ASYNC_PLAYER_WRITE"); }
    @Override public boolean isOp() { return server.getHandle().isOperator(profile); }
    @Override public void setOp(boolean op) {
        writable(); if (op == isOp()) return;
        if (op) server.getHandle().addToOperators(profile); else server.getHandle().removeFromOperators(profile);
        var player = getPlayer(); if (player != null) player.recalculatePermissions();
    }
    @Override public boolean isWhitelisted() { return server.getHandle().getWhitelist().isAllowed(profile); }
    @Override public void setWhitelisted(boolean value) {
        writable(); if (value) server.getHandle().getWhitelist().add(new net.minecraft.server.WhitelistEntry(profile));
        else server.getHandle().getWhitelist().remove(profile);
    }
    @Override public boolean isBanned() { return server.getHandle().getUserBanList().get(profile) != null; }
    @SuppressWarnings("unchecked")
    @Override public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E ban(String reason, java.util.Date expiration, String source) {
        writable(); var entry = new CraftProfileBanEntry(server, profile, new java.util.Date(), source, expiration, reason); entry.save(); return (E) entry;
    }
    @Override public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E ban(String reason, java.time.Instant expiration, String source) { return ban(reason, expiration == null ? null : java.util.Date.from(expiration), source); }
    @Override public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E ban(String reason, java.time.Duration duration, String source) { return ban(reason, duration == null ? (java.time.Instant) null : java.time.Instant.now().plus(duration), source); }
    private long timestamp(String section, String key) {
        var data = data(); if (data == null) return 0;
        var values = data.getCompound(section); if (values.contains(key)) return values.getLong(key);
        try { return Files.getLastModifiedTime(dataFile()).toMillis(); }
        catch (IOException failure) { throw new IllegalStateException("Cannot read player timestamp", failure); }
    }
    @Override public long getFirstPlayed() { var player = getPlayer(); return player == null ? timestamp("bukkit", "firstPlayed") : player.getFirstPlayed(); }
    @Override public long getLastPlayed() { var player = getPlayer(); return player == null ? timestamp("bukkit", "lastPlayed") : player.getLastPlayed(); }
    @Override public long getLastLogin() { var player = getPlayer(); return player == null ? timestamp("Paper", "LastLogin") : player.getLastLogin(); }
    @Override public long getLastSeen() { var player = getPlayer(); return player == null ? timestamp("Paper", "LastSeen") : player.getLastSeen(); }
    @Override public boolean hasPlayedBefore() { var player = getPlayer(); return player == null ? data() != null : player.hasPlayedBefore(); }
    @Override public Location getBedSpawnLocation() {
        var player = getPlayer(); if (player != null) return player.getBedSpawnLocation();
        var data = data(); if (data == null || !data.contains("SpawnX") || !data.contains("SpawnY") || !data.contains("SpawnZ")) return null;
        String dimension = data.contains("SpawnDimension") ? data.getString("SpawnDimension") : "minecraft:overworld";
        var world = server.getServer().getWorld(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, new net.minecraft.util.Identifier(dimension)));
        return world == null ? null : new Location(dev.meldrite.v1201.compat.NativeEntityExtensions.world(world), data.getInt("SpawnX"), data.getInt("SpawnY"), data.getInt("SpawnZ"));
    }
    @Override public Location getLastDeathLocation() {
        var player = getPlayer(); if (player != null) return player.getLastDeathLocation();
        var data = data(); if (data == null || !data.contains("LastDeathLocation")) return null;
        return net.minecraft.util.math.GlobalPos.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, data.get("LastDeathLocation")).result().map(pos -> {
            var world = server.getServer().getWorld(pos.getDimension());
            return world == null ? null : new Location(dev.meldrite.v1201.compat.NativeEntityExtensions.world(world), pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ());
        }).orElse(null);
    }
    private net.minecraft.stat.ServerStatHandler statistics(boolean write) {
        if (write) writable();
        var player = server.getHandle().getPlayer(getUniqueId()); if (player != null) return player.getStatHandler();
        var map = ((dev.meldrite.v1201.hook.PlayerManagerStatisticsAccess) server.getHandle()).meldrite$statistics();
        synchronized (map) {
            return map.computeIfAbsent(getUniqueId(), uuid -> new net.minecraft.stat.ServerStatHandler(server.getServer(), server.getServer().getSavePath(WorldSavePath.STATS).resolve(uuid + ".json").toFile()));
        }
    }
    private void saveStatistics(net.minecraft.stat.ServerStatHandler handler) { if (!isOnline()) handler.save(); }
    @Override public int getStatistic(Statistic statistic) { return CraftStatistic.getStatistic(statistics(false), statistic); }
    @Override public void setStatistic(Statistic statistic, int value) { var handler = statistics(true); CraftStatistic.setStatistic(handler, statistic, value); saveStatistics(handler); }
    @Override public void incrementStatistic(Statistic statistic) { incrementStatistic(statistic, 1); }
    @Override public void incrementStatistic(Statistic statistic, int value) { var handler = statistics(true); CraftStatistic.incrementStatistic(handler, statistic, value); saveStatistics(handler); }
    @Override public void decrementStatistic(Statistic statistic) { decrementStatistic(statistic, 1); }
    @Override public void decrementStatistic(Statistic statistic, int value) { var handler = statistics(true); CraftStatistic.decrementStatistic(handler, statistic, value); saveStatistics(handler); }
    @Override public int getStatistic(Statistic statistic, Material material) { return CraftStatistic.getStatistic(statistics(false), statistic, material); }
    @Override public void setStatistic(Statistic statistic, Material material, int value) { var handler = statistics(true); CraftStatistic.setStatistic(handler, statistic, material, value); saveStatistics(handler); }
    @Override public void incrementStatistic(Statistic statistic, Material material) { incrementStatistic(statistic, material, 1); }
    @Override public void incrementStatistic(Statistic statistic, Material material, int value) { var handler = statistics(true); CraftStatistic.incrementStatistic(handler, statistic, material, value); saveStatistics(handler); }
    @Override public void decrementStatistic(Statistic statistic, Material material) { decrementStatistic(statistic, material, 1); }
    @Override public void decrementStatistic(Statistic statistic, Material material, int value) { var handler = statistics(true); CraftStatistic.decrementStatistic(handler, statistic, material, value); saveStatistics(handler); }
    @Override public int getStatistic(Statistic statistic, EntityType entityType) { return CraftStatistic.getStatistic(statistics(false), statistic, entityType); }
    @Override public void setStatistic(Statistic statistic, EntityType entityType, int value) { var handler = statistics(true); CraftStatistic.setStatistic(handler, statistic, entityType, value); saveStatistics(handler); }
    @Override public void incrementStatistic(Statistic statistic, EntityType entityType) { incrementStatistic(statistic, entityType, 1); }
    @Override public void incrementStatistic(Statistic statistic, EntityType entityType, int value) { var handler = statistics(true); CraftStatistic.incrementStatistic(handler, statistic, entityType, value); saveStatistics(handler); }
    @Override public void decrementStatistic(Statistic statistic, EntityType entityType) { decrementStatistic(statistic, entityType, 1); }
    @Override public void decrementStatistic(Statistic statistic, EntityType entityType, int value) { var handler = statistics(true); CraftStatistic.decrementStatistic(handler, statistic, entityType, value); saveStatistics(handler); }
    @Override public Map<String, Object> serialize() { return Map.of("UUID", getUniqueId().toString()); }
    public static OfflinePlayer deserialize(Map<String, Object> values) {
        return values.containsKey("name") ? org.bukkit.Bukkit.getOfflinePlayer((String) values.get("name")) : org.bukkit.Bukkit.getOfflinePlayer(UUID.fromString((String) values.get("UUID")));
    }
    @Override public boolean equals(Object other) { return other instanceof OfflinePlayer player && getUniqueId().equals(player.getUniqueId()); }
    @Override public int hashCode() { return getUniqueId().hashCode(); }
    @Override public String toString() { return "CraftOfflinePlayer[UUID=" + getUniqueId() + "]"; }
}
