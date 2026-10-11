package dev.meldrite.v1201.compat;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/** Bukkit state and events around the vanilla player replacement lifecycle. */
public final class NativePlayerRespawn {
    private record Request(Location location, PlayerRespawnEvent.RespawnReason reason) { }
    private static final class Lifecycle {
        final ServerPlayerEntity previous;
        final CraftPlayer facade;
        final ServerWorld previousWorld;
        final boolean alive;
        final Request request;
        ServerPlayerEntity replacement;
        Lifecycle(ServerPlayerEntity previous, boolean alive, Request request) {
            this.previous = previous; this.alive = alive; this.request = request;
            this.previousWorld = previous.getServerWorld();
            this.facade = (CraftPlayer) NativeEntityExtensions.bukkit(previous);
        }
    }
    private static final ThreadLocal<Request> REQUEST = new ThreadLocal<>();
    private static final ThreadLocal<Lifecycle> CURRENT = new ThreadLocal<>();
    private NativePlayerRespawn() { }

    public static void respawn(CraftPlayer player) {
        var nativePlayer = player.getHandle();
        NativeEntityExtensions.requireThread(nativePlayer);
        if (nativePlayer.getHealth() > 0 || nativePlayer.networkHandler == null || !nativePlayer.networkHandler.isConnectionOpen()
                || nativePlayer.server.getPlayerManager().getPlayer(nativePlayer.getUuid()) != nativePlayer) return;
        requested(null, PlayerRespawnEvent.RespawnReason.PLUGIN,
                () -> nativePlayer.server.getPlayerManager().respawnPlayer(nativePlayer, false));
    }

    public static <T> T requested(Location location, PlayerRespawnEvent.RespawnReason reason, Supplier<T> action) {
        if (location != null) validate(location);
        Request previous = REQUEST.get();
        REQUEST.set(new Request(location == null ? null : location.clone(), Objects.requireNonNull(reason, "reason")));
        try { return action.get(); }
        finally { if (previous == null) REQUEST.remove(); else REQUEST.set(previous); }
    }

    /** Called for every native respawn, including normal client death and End return. */
    public static ServerPlayerEntity lifecycle(ServerPlayerEntity previous, boolean alive, Supplier<ServerPlayerEntity> action) {
        if (!NativeEntityExtensions.eventsAvailable()) return action.get();
        NativeEntityExtensions.requireThread(previous);
        Lifecycle outer = CURRENT.get();
        Lifecycle current = new Lifecycle(previous, alive, REQUEST.get());
        CURRENT.set(current);
        try {
            ServerPlayerEntity result = action.get();
            if (current.replacement != result) throw new IllegalStateException("NATIVE_RESPAWN_PREPARATION_MISSING");
            current.facade.meldrite$restoreClientState();
            if (result.getServerWorld() != current.previousWorld) {
                current.facade.getServer().getPluginManager().callEvent(new PlayerChangedWorldEvent(current.facade,
                        NativeEntityExtensions.world(current.previousWorld)));
            }
            return result;
        } finally { if (outer == null) CURRENT.remove(); else CURRENT.set(outer); }
    }

    /** Runs after vanilla copy/position selection, before packets and world insertion. */
    public static ServerWorld prepare(ServerPlayerEntity replacement, ServerWorld destinationWorld,
            ServerWorld spawnWorld, Optional<Vec3d> respawnPosition) {
        Lifecycle current = CURRENT.get();
        if (current == null) return destinationWorld;
        if (current.replacement != null) throw new IllegalStateException("NATIVE_RESPAWN_PREPARED_TWICE");
        if (!current.previous.getUuid().equals(replacement.getUuid())) throw new IllegalStateException("NATIVE_RESPAWN_IDENTITY_CHANGED");
        NativeEntityExtensions.rebind(current.facade, current.previous, replacement);
        replacement.networkHandler.player = replacement;
        Location location = current.request != null && current.request.location() != null
                ? current.request.location().clone() : current.facade.getLocation();
        var spawnPoint = current.previous.getSpawnPointPosition();
        var block = spawnWorld != null && spawnPoint != null && respawnPosition.isPresent() ? spawnWorld.getBlockState(spawnPoint) : null;
        boolean bed = block != null && block.getBlock() instanceof BedBlock;
        boolean anchor = block != null && block.isOf(Blocks.RESPAWN_ANCHOR);
        var reason = current.request == null ? (current.alive ? PlayerRespawnEvent.RespawnReason.END_PORTAL : PlayerRespawnEvent.RespawnReason.DEATH)
                : current.request.reason();
        var event = new PlayerRespawnEvent(current.facade, location, bed, anchor, reason);
        current.facade.getServer().getPluginManager().callEvent(event);
        location = event.getRespawnLocation();
        ServerWorld selected = validate(location);
        if (selected.getServer() != replacement.server) throw new IllegalArgumentException("RESPAWN_REQUIRES_SAME_SERVER");
        if (selected != replacement.getServerWorld()) {
            replacement.setServerWorld(selected);
            replacement.interactionManager.setWorld(selected);
            // The event may change dimensions after the first native replacement publication.
            current.facade.meldrite$rebindNative(replacement);
        }
        replacement.refreshPositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        // Event destinations need the same native suffocation escape as the initial spawn.
        while (!selected.isSpaceEmpty(replacement) && replacement.getY() < selected.getTopY()) {
            replacement.setPosition(replacement.getX(), replacement.getY() + 1, replacement.getZ());
        }
        current.replacement = replacement;
        return selected;
    }

    private static ServerWorld validate(Location location) {
        Objects.requireNonNull(location, "location").checkFinite();
        if (!(location.getWorld() instanceof CraftWorld world)) throw new IllegalArgumentException("RESPAWN_REQUIRES_NATIVE_WORLD");
        ServerWorld nativeWorld = world.getHandle();
        NativeEntityExtensions.requireThread(nativeWorld);
        return nativeWorld;
    }
}
