package org.bukkit.craftbukkit.v1_20_R1.ban;

import com.mojang.authlib.GameProfile;
import java.util.Date;
import dev.meldrite.paper.facade.PlayerProfileAdapter;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;

public final class CraftProfileBanEntry implements org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> {
    private final CraftServer server;
    private final net.minecraft.server.BannedPlayerList list;
    private final GameProfile profile;
    private Date created;
    private Date expiration;
    private String source;
    private String reason;
    public CraftProfileBanEntry(CraftServer server, GameProfile profile, Date created, String source, Date expiration, String reason) {
        this.server = java.util.Objects.requireNonNull(server); this.list = server.getHandle().getUserBanList(); this.profile = profile; this.created = copy(created); this.source = source; this.expiration = copy(expiration); this.reason = reason;
    }
    public CraftProfileBanEntry(GameProfile profile, net.minecraft.server.BannedPlayerEntry entry, net.minecraft.server.BannedPlayerList list) {
        this.server = (CraftServer) org.bukkit.Bukkit.getServer(); this.profile = profile; this.list = java.util.Objects.requireNonNull(list);
        this.created = copy(entry.getCreationDate()); this.expiration = copy(entry.getExpiryDate()); this.source = entry.getSource(); this.reason = entry.getReason();
    }
    private static Date copy(Date value) { return value == null ? null : new Date(value.getTime()); }
    @Override public String getTarget() { return profile.getName(); }
    @Override public com.destroystokyo.paper.profile.PlayerProfile getBanTarget() { return new PlayerProfileAdapter(profile); }
    @Override public Date getCreated() { return copy(created); }
    @Override public void setCreated(Date value) { created = copy(value); }
    @Override public Date getExpiration() { return copy(expiration); }
    @Override public void setExpiration(Date value) { expiration = value != null && value.equals(Date.from(java.time.Instant.parse("1899-12-31T04:00:00Z"))) ? null : copy(value); }
    @Override public String getSource() { return source; }
    @Override public void setSource(String value) { source = value; }
    @Override public String getReason() { return reason; }
    @Override public void setReason(String value) { reason = value; }
    @Override public void save() {
        if (server != null && !server.getServer().isOnThread()) throw new IllegalStateException("ASYNC_PLAYER_WRITE");
        list.add(new net.minecraft.server.BannedPlayerEntry(profile, created, source, expiration, reason));
    }
    @Override public void remove() {
        if (server != null && !server.getServer().isOnThread()) throw new IllegalStateException("ASYNC_PLAYER_WRITE");
        list.remove(profile);
    }
}
