package org.bukkit.craftbukkit.v1_20_R1.ban;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;

/** Paper/Bukkit IP ban API projected from the native server list. */
public final class CraftIpBanList implements org.bukkit.ban.IpBanList {
    private final BannedIpList list;
    public CraftIpBanList(BannedIpList list) { this.list = java.util.Objects.requireNonNull(list); }
    @Override public org.bukkit.BanEntry<InetAddress> getBanEntry(String target) {
        java.util.Objects.requireNonNull(target, "target");
        BannedIpEntry entry = list.get(target);
        return entry == null ? null : new CraftIpBanEntry(target, entry, list);
    }
    @Override public org.bukkit.BanEntry<InetAddress> getBanEntry(InetAddress target) { return getBanEntry(ip(target)); }
    @Override public org.bukkit.BanEntry<InetAddress> addBan(String target, String reason, Date expires, String source) {
        java.util.Objects.requireNonNull(target, "target");
        BannedIpEntry entry = new BannedIpEntry(target, new Date(), blank(source), expires, blank(reason));
        list.add(entry);
        return new CraftIpBanEntry(target, entry, list);
    }
    @Override public org.bukkit.BanEntry<InetAddress> addBan(InetAddress target, String reason, Date expires, String source) { return addBan(ip(target), reason, expires, source); }
    @Override public org.bukkit.BanEntry<InetAddress> addBan(InetAddress target, String reason, Instant expires, String source) { return addBan(target, reason, expires == null ? null : Date.from(expires), source); }
    @Override public org.bukkit.BanEntry<InetAddress> addBan(InetAddress target, String reason, Duration duration, String source) { return addBan(target, reason, duration == null ? null : Date.from(Instant.now().plus(duration)), source); }
    @Override public Set<org.bukkit.BanEntry> getBanEntries() { return new LinkedHashSet<>(getEntries()); }
    @Override public Set<org.bukkit.BanEntry<InetAddress>> getEntries() {
        Set<org.bukkit.BanEntry<InetAddress>> result = new LinkedHashSet<>();
        for (String target : list.getNames()) { var entry = list.get(target); if (entry != null) result.add(new CraftIpBanEntry(target, entry, list)); }
        return result;
    }
    @Override public boolean isBanned(InetAddress target) { return isBanned(ip(target)); }
    @Override public boolean isBanned(String target) { return target != null && list.isBanned(target); }
    @Override public void pardon(InetAddress target) { pardon(ip(target)); }
    @Override public void pardon(String target) { if (target != null) list.remove(target); }
    private static String ip(InetAddress target) { return target == null ? null : InetAddresses.toAddrString(target); }
    private static String blank(String value) { return value == null || value.isBlank() ? null : value; }
}
