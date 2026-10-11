package org.bukkit.craftbukkit.v1_20_R1.ban;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Date;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;

/** Bukkit view backed by one native IP ban entry. */
public final class CraftIpBanEntry implements org.bukkit.BanEntry<InetAddress> {
    private static final long FOREVER_SENTINEL = Instant.parse("1899-12-31T04:00:00Z").toEpochMilli();
    private final BannedIpList list;
    private final String target;
    private Date created;
    private Date expiration;
    private String source;
    private String reason;

    public CraftIpBanEntry(String target, BannedIpEntry entry, BannedIpList list) {
        this.target = java.util.Objects.requireNonNull(target);
        this.list = java.util.Objects.requireNonNull(list);
        this.created = copy(entry.getCreationDate());
        this.expiration = copy(entry.getExpiryDate());
        this.source = entry.getSource();
        this.reason = entry.getReason();
    }

    @Override public String getTarget() { return target; }
    @Override public InetAddress getBanTarget() { return InetAddresses.forString(target); }
    @Override public Date getCreated() { return copy(created); }
    @Override public void setCreated(Date value) { created = copy(value); }
    @Override public Date getExpiration() { return copy(expiration); }
    @Override public void setExpiration(Date value) { expiration = value != null && value.getTime() == FOREVER_SENTINEL ? null : copy(value); }
    @Override public String getSource() { return source; }
    @Override public void setSource(String value) { source = value; }
    @Override public String getReason() { return reason; }
    @Override public void setReason(String value) { reason = value; }
    @Override public void save() { list.add(new BannedIpEntry(target, created, source, expiration, reason)); }
    @Override public void remove() { list.remove(target); }

    private static Date copy(Date value) { return value == null ? null : new Date(value.getTime()); }
}
