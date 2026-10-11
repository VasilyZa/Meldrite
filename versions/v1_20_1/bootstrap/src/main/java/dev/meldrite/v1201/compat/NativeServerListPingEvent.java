package dev.meldrite.v1201.compat;

import com.mojang.authlib.GameProfile;
import java.net.InetAddress;
import java.util.*;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftIconCache;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

/** Per-request player snapshot. Removing a player changes this response, not the server roster. */
public final class NativeServerListPingEvent extends ServerListPingEvent {
    private final List<Player> players;
    private final Set<UUID> removed = new HashSet<>();
    private byte[] icon;

    public NativeServerListPingEvent(String hostname, InetAddress address, ServerMetadata metadata,
                                    Collection<? extends Player> players) {
        super(hostname, address, GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(metadata.description())),
                metadata.players().map(ServerMetadata.Players::max).orElse(0));
        this.players = List.copyOf(players);
        icon = metadata.favicon().map(value -> value.iconBytes().clone()).orElse(null);
    }

    @Override public Iterator<Player> iterator() {
        // Each iterator snapshots the visible players; a later iterator observes all removals.
        var visible = players.stream().filter(player -> !removed.contains(player.getUniqueId())).toList();
        return new Iterator<>() {
            private int index;
            private Player current;
            @Override public boolean hasNext() { return index < visible.size(); }
            @Override public Player next() {
                if (!hasNext()) throw new NoSuchElementException();
                return current = visible.get(index++);
            }
            @Override public void remove() {
                if (current == null) throw new IllegalStateException("next() must precede remove()");
                removed.add(current.getUniqueId()); current = null;
            }
        };
    }

    @Override public void setServerIcon(CachedServerIcon icon) {
        if (icon != null && !(icon instanceof CraftIconCache)) throw new IllegalArgumentException("Icon must be loaded by this server");
        byte[] bytes = icon == null ? null : ((CraftIconCache) icon).value;
        this.icon = bytes == null ? null : bytes.clone();
    }

    /** Builds an independent native response while retaining version and secure-chat metadata. */
    public ServerMetadata response(ServerMetadata original, boolean hideOnlinePlayers) {
        var knownProfiles = new HashMap<UUID, GameProfile>();
        original.players().ifPresent(value -> value.sample().forEach(profile -> knownProfiles.put(profile.getId(), profile)));
        var sample = new ArrayList<GameProfile>();
        if (!hideOnlinePlayers) for (Player player : this) {
            GameProfile profile = knownProfiles.get(player.getUniqueId());
            if (player instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer craftPlayer && !craftPlayer.getHandle().allowsServerListing()) {
                profile = net.minecraft.server.MinecraftServer.ANONYMOUS_PLAYER_PROFILE;
            } else if (profile == null) profile = new GameProfile(player.getUniqueId(), player.getName());
            sample.add(profile);
            if (sample.size() == 12) break;
        }
        Text description = Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(motd()));
        return new ServerMetadata(description,
                Optional.of(new ServerMetadata.Players(getMaxPlayers(), getNumPlayers(), List.copyOf(sample))),
                original.version(), icon == null ? Optional.empty() : Optional.of(new ServerMetadata.Favicon(icon.clone())),
                original.secureChatEnforced());
    }
}
