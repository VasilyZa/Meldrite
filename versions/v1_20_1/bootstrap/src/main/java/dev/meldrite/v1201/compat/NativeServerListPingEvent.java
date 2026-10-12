package dev.meldrite.v1201.compat;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.network.StatusClient;
import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.authlib.GameProfile;
import dev.meldrite.paper.facade.PlayerProfileAdapter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftIconCache;
import org.bukkit.entity.Player;
import org.bukkit.util.CachedServerIcon;

/** Per-request player snapshot. Removing a player changes this response, not the server roster. */
public final class NativeServerListPingEvent extends PaperServerListPingEvent {
    private final List<Player> players;

    public NativeServerListPingEvent(String hostname, InetAddress address, ServerMetadata metadata,
                                    Collection<? extends Player> players) {
        this(new StatusClient() {
            @Override public InetSocketAddress getAddress() { return new InetSocketAddress(address, 0); }
            @Override public int getProtocolVersion() { return -1; }
            @Override public InetSocketAddress getVirtualHost() {
                return hostname == null || hostname.isEmpty() ? null : InetSocketAddress.createUnresolved(hostname, 0);
            }
        }, metadata, players);
    }

    public NativeServerListPingEvent(StatusClient client, ServerMetadata metadata,
                                    Collection<? extends Player> players) {
        super(Objects.requireNonNull(client, "client"),
                GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(metadata.description())),
                metadata.players().map(ServerMetadata.Players::online).orElse(players.size()),
                metadata.players().map(ServerMetadata.Players::max).orElse(0),
                metadata.version().map(ServerMetadata.Version::gameVersion).orElse(""),
                metadata.version().map(ServerMetadata.Version::protocolVersion).orElse(-1),
                metadata.favicon().map(value -> new CraftIconCache(value.iconBytes())).orElse(null));
        this.players = List.copyOf(players);
        metadata.players().ifPresent(value -> value.sample().forEach(profile -> getPlayerSample().add(new CraftPlayerProfile(profile))));
        setHidePlayers(metadata.players().isEmpty());
    }

    @Override public String getHostname() {
        InetSocketAddress host = getClient().getVirtualHost();
        return host == null ? "" : host.getHostString();
    }

    /** Paper owns iterator removal, advertised count adjustment and sample UUID filtering. */
    @Override protected Object[] getOnlinePlayers() { return players.toArray(); }

    @Override public void setServerIcon(CachedServerIcon icon) {
        if (icon != null && !(icon instanceof CraftIconCache)) throw new IllegalArgumentException("Icon must be loaded by this server");
        byte[] bytes = icon == null ? null : ((CraftIconCache) icon).value;
        super.setServerIcon(bytes == null ? null : new CraftIconCache(bytes));
    }

    /**
     * Builds a detached native response from final Paper state, or null for cancellation.
     * hideOnlinePlayers requests an additional sample filter; the normal Paper bridge
     * uses the native initial sample and final plugin edits without this extra filter.
     */
    public ServerMetadata response(ServerMetadata original, boolean hideOnlinePlayers) {
        if (isCancelled()) return null;
        var sample = hideOnlinePlayers ? List.<GameProfile>of()
                : getPlayerSample().stream().map(NativeServerListPingEvent::nativeSample).toList();
        Text description = Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(motd()));
        byte[] icon = getServerIcon() == null ? null : ((CraftIconCache) getServerIcon()).value;
        var version = original.version().isEmpty() && getVersion().isEmpty() && getProtocolVersion() == -1
                ? Optional.<ServerMetadata.Version>empty()
                : Optional.of(new ServerMetadata.Version(getVersion(), getProtocolVersion()));
        return new ServerMetadata(description,
                shouldHidePlayers() ? Optional.empty() : Optional.of(new ServerMetadata.Players(getMaxPlayers(), getNumPlayers(), sample)),
                version, icon == null ? Optional.empty() : Optional.of(new ServerMetadata.Favicon(icon.clone())),
                original.secureChatEnforced());
    }

    private static GameProfile nativeSample(PlayerProfile profile) {
        if (profile.getId() != null && profile.getName() != null) return PlayerProfileAdapter.from(profile);
        // Paper 1.20.1 uses a nil UUID and empty name for display-only sample entries.
        // These are protocol placeholders; no player lookup or profile completion runs.
        return new GameProfile(profile.getId() == null ? net.minecraft.util.Util.NIL_UUID : profile.getId(),
                Objects.requireNonNullElse(profile.getName(), ""));
    }
}
