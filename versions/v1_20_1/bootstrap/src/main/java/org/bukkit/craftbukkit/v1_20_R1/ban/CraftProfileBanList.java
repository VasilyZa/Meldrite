package org.bukkit.craftbukkit.v1_20_R1.ban;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import org.bukkit.profile.PlayerProfile;

/** Paper profile ban API projected from the native user ban list. */
public final class CraftProfileBanList implements org.bukkit.ban.ProfileBanList {
    private final BannedPlayerList list;
    private final org.bukkit.craftbukkit.v1_20_R1.CraftServer server;
    private final Map<UUID, GameProfile> knownProfiles = new ConcurrentHashMap<>();
    public CraftProfileBanList(BannedPlayerList list) { this(null, list); }
    public CraftProfileBanList(org.bukkit.craftbukkit.v1_20_R1.CraftServer server, BannedPlayerList list) {
        this.server = server;
        this.list = java.util.Objects.requireNonNull(list);
    }
    @Override public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> getBanEntry(String target) { return getBanEntry(resolve(target)); }
    @Override public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> getBanEntry(PlayerProfile target) { return getBanEntry(profile(target)); }
    @Override public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> getBanEntry(com.destroystokyo.paper.profile.PlayerProfile target) { return getBanEntry(profile(target)); }
    public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> getBanEntry(GameProfile target) {
        if (target == null) return null;
        remember(target);
        BannedPlayerEntry entry = list.get(target);
        return entry == null ? null : new CraftProfileBanEntry(target, entry, list);
    }
    @Override public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(String target, String reason, Date expires, String source) { return addBan(resolve(target), reason, expires, source); }
    @Override public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(PlayerProfile target, String reason, Date expires, String source) { return addBan(profile(target), reason, expires, source); }
    @Override public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(com.destroystokyo.paper.profile.PlayerProfile target, String reason, Date expires, String source) { return addBan(profile(target), reason, expires, source); }
    public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(GameProfile profile, String reason, Date expires, String source) {
        if (profile == null) return null;
        profile = normalized(profile);
        remember(profile);
        BannedPlayerEntry entry = new BannedPlayerEntry(profile, new Date(), blank(source), expires, blank(reason));
        list.add(entry);
        return new CraftProfileBanEntry(profile, entry, list);
    }
    @Override public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(PlayerProfile target, String reason, Instant expires, String source) { return addBan(target, reason, expires == null ? null : Date.from(expires), source); }
    @Override public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(PlayerProfile target, String reason, Duration duration, String source) { return addBan(target, reason, duration == null ? null : Date.from(Instant.now().plus(duration)), source); }
    @Override public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(com.destroystokyo.paper.profile.PlayerProfile target, String reason, Instant expires, String source) { return addBan(target, reason, expires == null ? null : Date.from(expires), source); }
    @Override public org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(com.destroystokyo.paper.profile.PlayerProfile target, String reason, Duration duration, String source) { return addBan(target, reason, duration == null ? null : Date.from(Instant.now().plus(duration)), source); }
    @Override public Set<org.bukkit.BanEntry> getBanEntries() { return new LinkedHashSet<>(getEntries()); }
    @Override public Set<org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile>> getEntries() {
        Set<org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile>> result = new LinkedHashSet<>();
        for (String name : list.getNames()) {
            GameProfile profile = profileForName(name);
            BannedPlayerEntry entry = profile == null ? null : list.get(profile);
            if (entry != null) result.add(new CraftProfileBanEntry(profile, entry, list));
        }
        return result;
    }
    @Override public boolean isBanned(PlayerProfile target) { return isBanned(profile(target)); }
    @Override public boolean isBanned(com.destroystokyo.paper.profile.PlayerProfile target) { return isBanned(profile(target)); }
    public boolean isBanned(GameProfile target) { return target != null && list.contains(normalized(target)); }
    @Override public boolean isBanned(String target) { return isBanned(resolve(target)); }
    @Override public void pardon(PlayerProfile target) { pardon(profile(target)); }
    @Override public void pardon(com.destroystokyo.paper.profile.PlayerProfile target) { pardon(profile(target)); }
    public void pardon(GameProfile target) { if (target != null) list.remove(normalized(target)); }
    @Override public void pardon(String target) { pardon(resolve(target)); }
    private static GameProfile profile(PlayerProfile value) { return value == null ? null : dev.meldrite.paper.facade.PlayerProfileAdapter.from(value); }
    private static GameProfile profile(com.destroystokyo.paper.profile.PlayerProfile value) { return value == null ? null : dev.meldrite.paper.facade.PlayerProfileAdapter.from(value); }
    private GameProfile resolve(String value) {
        if (value == null) return null;
        try { return knownProfiles.getOrDefault(UUID.fromString(value), new GameProfile(UUID.fromString(value), null)); }
        catch (IllegalArgumentException ignored) {
            String name = value.trim();
            if (server != null) {
                org.bukkit.OfflinePlayer player = server.getOfflinePlayer(name);
                GameProfile profile = new GameProfile(player.getUniqueId(), player.getName());
                remember(profile);
                return profile;
            }
            UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return knownProfiles.computeIfAbsent(id, key -> new GameProfile(key, name));
        }
    }
    private GameProfile normalized(GameProfile profile) {
        if (profile.getId() != null) return profile;
        return resolve(profile.getName());
    }
    private GameProfile profileForName(String name) {
        for (GameProfile profile : knownProfiles.values()) {
            if (java.util.Objects.equals(profile.getName(), name)
                    || profile.getName() != null && profile.getName().equalsIgnoreCase(name)) return profile;
        }
        return resolve(name);
    }
    private void remember(GameProfile profile) { if (profile != null && profile.getId() != null) knownProfiles.put(profile.getId(), profile); }
    private static String blank(String value) { return value == null || value.isBlank() ? null : value; }
}
