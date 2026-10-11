package dev.meldrite.v1201.compat;

import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/** Per-connection Bukkit decisions after vanilla movement validation. */
public final class NativePlayerMovement {
    private Location lastEventLocation;
    private long teleportVersion;

    /** Teleports start a new movement origin and invalidate packets inside listeners. */
    public void reset() {
        lastEventLocation = null;
        teleportVersion++;
    }

    public boolean move(Player player, Location before, Location requested, Consumer<Location> correctPosition) {
        if (lastEventLocation == null || !Objects.equals(lastEventLocation.getWorld(), before.getWorld())) {
            lastEventLocation = before.clone();
        }
        Location from = lastEventLocation.clone();
        double dx = from.getX() - requested.getX();
        double dy = from.getY() - requested.getY();
        double dz = from.getZ() - requested.getZ();
        float angle = Math.abs(from.getYaw() - requested.getYaw()) + Math.abs(from.getPitch() - requested.getPitch());
        if (dx * dx + dy * dy + dz * dz <= 1.0 / 256.0 && angle <= 10.0f) return true;

        lastEventLocation = requested.clone();
        long version = teleportVersion;
        var event = new PlayerMoveEvent(player, from, requested.clone());
        player.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getTo() == null) {
            // Correction must not fire PlayerTeleportEvent and be cancelled again.
            correctPosition.accept(event.getFrom());
            reset();
            return false;
        }
        if (!requested.equals(event.getTo())) {
            player.teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            return false;
        }
        return version == teleportVersion && before.equals(player.getLocation());
    }
}
