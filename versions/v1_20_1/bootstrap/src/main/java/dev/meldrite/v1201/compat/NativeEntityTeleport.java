package dev.meldrite.v1201.compat;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/** Bukkit teleport events and facade identity around the native transfer operation. */
public final class NativeEntityTeleport {
    private static final class Transfer {
        final CraftEntity facade;
        final Entity previous;
        final ServerWorld previousWorld;
        Entity replacement;
        Transfer(CraftEntity facade, Entity previous) {
            this.facade = facade; this.previous = previous;
            this.previousWorld = (ServerWorld) previous.getWorld();
        }
    }
    private static final ThreadLocal<Transfer> CURRENT = new ThreadLocal<>();
    private NativeEntityTeleport() { }

    /** Chunk loading may finish on a worker; transfer and future completion run on the server thread. */
    public static CompletableFuture<Boolean> teleportAsync(CraftEntity facade, Location requested, PlayerTeleportEvent.TeleportCause cause) {
        Objects.requireNonNull(cause, "cause");
        Location destination = Objects.requireNonNull(requested, "location").clone();
        destination.checkFinite();
        if (!(destination.getWorld() instanceof CraftWorld world)) throw new IllegalArgumentException("TELEPORT_REQUIRES_NATIVE_WORLD");
        MinecraftServer server = world.getHandle().getServer();
        if (server != facade.getHandle().getServer()) throw new IllegalArgumentException("TELEPORT_REQUIRES_SAME_SERVER");
        var result = new CompletableFuture<Boolean>();
        try {
            world.getChunkAtAsyncUrgently(destination).whenComplete((chunk, failure) -> finishAsync(server, result, () -> {
                if (failure != null) result.completeExceptionally(failure);
                else result.complete(chunk != null && teleport(facade, destination, cause));
            }));
        } catch (Throwable failure) {
            finishAsync(server, result, () -> result.completeExceptionally(failure));
        }
        return result;
    }

    private static void finishAsync(MinecraftServer server, CompletableFuture<Boolean> result, Runnable completion) {
        try {
            server.execute(() -> {
                if (result.isDone()) return;
                try { completion.run(); }
                catch (Throwable failure) { result.completeExceptionally(failure); }
            });
        } catch (Throwable failure) { result.completeExceptionally(failure); }
    }

    public static boolean teleport(CraftEntity facade, Location requested, PlayerTeleportEvent.TeleportCause cause) {
        Objects.requireNonNull(cause, "cause");
        Location destination = Objects.requireNonNull(requested, "location").clone();
        ServerWorld target = world(destination);
        Entity entity = facade.getHandle();
        NativeEntityExtensions.requireThread(entity);
        if (target.getServer() != entity.getServer()) throw new IllegalArgumentException("TELEPORT_REQUIRES_SAME_SERVER");
        if (entity.isRemoved() || !entity.isAlive() || entity.hasPassengers()) return false;
        Location from = facade.getLocation();
        if (facade instanceof CraftPlayer player) {
            var event = new PlayerTeleportEvent(player, from, destination, cause);
            facade.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getTo() == null) return false;
            destination = event.getTo().clone();
        } else {
            var event = new EntityTeleportEvent(facade, from, destination);
            facade.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getTo() == null) return false;
            destination = event.getTo().clone();
        }
        target = world(destination);
        if (target.getServer() != entity.getServer()) throw new IllegalArgumentException("TELEPORT_REQUIRES_SAME_SERVER");
        if (entity.isRemoved() || facade.getHandle() != entity || entity.hasPassengers()) return false;
        if (facade instanceof CraftPlayer player && ((ServerPlayerEntity) entity).currentScreenHandler != ((ServerPlayerEntity) entity).playerScreenHandler)
            player.closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason.TELEPORT);
        var previous = CURRENT.get();
        var transfer = new Transfer(facade, entity);
        CURRENT.set(transfer);
        try {
            entity.stopRiding();
            if (entity.hasVehicle()) return false;
            boolean success = entity.teleport(target, destination.getX(), destination.getY(), destination.getZ(),
                    Set.of(), destination.getYaw(), destination.getPitch());
            if (!success) return false;
            if (entity instanceof ServerPlayerEntity player && player.getServerWorld() != transfer.previousWorld) {
                if (transfer.replacement == null) playerWorldChanged(player);
                ((CraftPlayer) facade).meldrite$restoreClientState();
                facade.getServer().getPluginManager().callEvent(new PlayerChangedWorldEvent((CraftPlayer) facade,
                        NativeEntityExtensions.world(transfer.previousWorld)));
            } else if (!(entity instanceof ServerPlayerEntity) && target != transfer.previousWorld && transfer.replacement == null) {
                if (entity.getWorld() == target && !entity.isRemoved()) entityWorldChanged(entity);
                else throw new IllegalStateException("NATIVE_TELEPORT_REBIND_MISSING");
            }
            return true;
        } finally {
            if (previous == null) CURRENT.remove(); else CURRENT.set(previous);
        }
    }

    /** Called before a cloned entity enters the destination world's tracker. */
    public static void beforeDimensionInsert(Entity replacement) {
        Transfer transfer = CURRENT.get();
        if (transfer == null || transfer.previous instanceof ServerPlayerEntity || !replacement.getUuid().equals(transfer.previous.getUuid())) return;
        if (transfer.replacement != null) throw new IllegalStateException("NATIVE_TELEPORT_REBOUND_TWICE");
        NativeEntityExtensions.rebind(transfer.facade, transfer.previous, replacement);
        transfer.replacement = replacement;
    }

    /** Native portals and teleports also clone entities without a Bukkit teleport in progress. */
    public static void beforeDimensionInsert(Entity previous, Entity replacement) {
        Transfer transfer = CURRENT.get();
        if (transfer != null && transfer.previous == previous) {
            beforeDimensionInsert(replacement);
            return;
        }
        if (previous instanceof ServerPlayerEntity || !(previous instanceof PaperEntityAccess access)) return;
        CraftEntity facade = access.meldrite$getCachedBukkitEntity();
        if (facade != null) NativeEntityExtensions.rebind(facade, previous, replacement);
    }

    /** A player transfer retains the same native object but changes its world identity. */
    public static void playerWorldChanged(ServerPlayerEntity player) {
        Transfer transfer = CURRENT.get();
        if (transfer == null || transfer.previous != player || player.getServerWorld() == transfer.previousWorld) return;
        transfer.facade.meldrite$rebindNative(player);
        transfer.replacement = player;
    }
    public static void entityWorldChanged(Entity entity) {
        Transfer transfer = CURRENT.get();
        if (transfer == null || transfer.previous != entity || entity instanceof ServerPlayerEntity || entity.getWorld() == transfer.previousWorld) return;
        transfer.facade.meldrite$rebindNative(entity);
        transfer.replacement = entity;
    }

    private static ServerWorld world(Location location) {
        location.checkFinite();
        if (!(location.getWorld() instanceof CraftWorld world)) throw new IllegalArgumentException("TELEPORT_REQUIRES_NATIVE_WORLD");
        NativeEntityExtensions.requireThread(world.getHandle());
        return world.getHandle();
    }
}
