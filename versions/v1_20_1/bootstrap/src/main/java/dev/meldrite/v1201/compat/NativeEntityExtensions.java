package dev.meldrite.v1201.compat;

import dev.meldrite.foundation.EntityHandle;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.bridge.NativeWorldPort;
import dev.meldrite.v1201.hook.ServerWorldPaperAccessMixin;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.spigotmc.event.entity.EntityDismountEvent;

/** Connects Craft/Paper entity extensions to the live native entity and world. */
public final class NativeEntityExtensions {
    private record Binding(FacadeContext context, NativeEntityPort entities) { }
    private static volatile Binding binding;
    private static final ThreadLocal<Entity> FORCED_DISMOUNT = new ThreadLocal<>();

    private NativeEntityExtensions() { }
    public static void bind(FacadeContext context, NativeEntityPort entities) {
        binding = new Binding(Objects.requireNonNull(context), Objects.requireNonNull(entities));
    }

    public static CraftEntity bukkit(Entity entity) {
        Binding active = requireBinding();
        // Native removal does not end the Bukkit object's lifetime. A dead player
        // remains in the online roster until respawn, which rebinds this facade.
        // Reuse published identity; projecting a new removed entity still rejects below.
        if (entity instanceof PaperEntityAccess access) {
            CraftEntity cached = access.meldrite$getCachedBukkitEntity();
            if (cached != null && cached.meldrite$isCachedFor(active.context, entity)) return cached;
        }
        requireThread(entity);
        if (!(entity.getWorld() instanceof ServerWorld world)) throw new IllegalStateException("ENTITY_NOT_IN_SERVER_WORLD");
        var handle = active.entities.project(entity);
        var facade = (CraftEntity) active.context.projectEntity(handle, entity.getId(), entity instanceof ServerPlayerEntity,
                entity.getName().getString());
        if (entity instanceof PaperEntityAccess access) access.meldrite$setCachedBukkitEntity(facade);
        return facade;
    }

    /** Resolves a facade identity to the live native object for typed API adapters. */
    public static Entity require(EntityHandle handle) {
        return requireBinding().entities.require(handle);
    }
    public static void rebind(CraftEntity facade, Entity previous, Entity replacement) {
        Binding active = requireBinding();
        requireThread(replacement);
        if (!facade.meldrite$isCachedFor(active.context, previous)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE");
        if (!previous.getUuid().equals(replacement.getUuid())) throw new IllegalArgumentException("ENTITY_REBIND_IDENTITY_MISMATCH");
        state(replacement).meldrite$importSessionState(state(previous).meldrite$exportSessionState());
        if (previous instanceof PaperLivingPickupAccess source && replacement instanceof PaperLivingPickupAccess target) {
            target.meldrite$setPickupOverride(source.meldrite$getPickupOverride());
        }
        if (previous instanceof PaperItemAccess source && replacement instanceof PaperItemAccess target) {
            target.meldrite$setCanMobPickup(source.meldrite$canMobPickup());
            target.meldrite$setFrictionState(source.meldrite$getFrictionState());
        }
        facade.meldrite$rebindNative(replacement);
        state(previous).meldrite$setCachedBukkitEntity(null);
        state(replacement).meldrite$setCachedBukkitEntity(facade);
    }

    public static CraftWorld world(ServerWorld world) {
        requireThread(world);
        var context = requireBinding().context;
        var key = NativeWorldPort.key(world).key();
        var result = context.worldByKey(new org.bukkit.NamespacedKey(key.entryNamespace(), key.entryPath()));
        if (!(result instanceof CraftWorld craft)) throw new IllegalStateException("WORLD_FACADE_NOT_PRESENT");
        return craft;
    }

    public static Object lookup(Object value) {
        if (!(value instanceof ServerWorld world)) throw new IllegalArgumentException("NOT_SERVER_WORLD");
        requireThread(world);
        return new io.papermc.paper.chunk.system.entity.EntityLookup(world,
                ((ServerWorldPaperAccessMixin) world).meldrite$entityLookup());
    }

    public static void dismount(Entity entity, boolean suppressCancellation) {
        requireThread(entity);
        if (!entity.hasVehicle()) return;
        if (!suppressCancellation) {
            entity.stopRiding();
            return;
        }
        Entity previous = FORCED_DISMOUNT.get();
        FORCED_DISMOUNT.set(entity);
        try { entity.stopRiding(); }
        finally {
            if (previous == null) FORCED_DISMOUNT.remove(); else FORCED_DISMOUNT.set(previous);
        }
    }

    /** Vanilla clears the rider link before removing it from the vehicle's passenger list. */
    public static boolean allowDismount(Entity entity, Entity vehicle) {
        if (!(entity.getWorld() instanceof ServerWorld) || !eventsAvailable()
                || !state(entity).meldrite$isTracked()) return true;
        requireThread(entity);
        boolean cancellable = FORCED_DISMOUNT.get() != entity && !entity.isRemoved() && !vehicle.isRemoved();
        var event = new EntityDismountEvent(bukkit(entity), bukkit(vehicle), cancellable);
        requireBinding().context.server().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static void teleport(ServerPlayNetworkHandler connection, Location destination) {
        Objects.requireNonNull(destination).checkFinite();
        requireThread(connection.player);
        if (!(destination.getWorld() instanceof CraftWorld world) || world.getHandle() != connection.player.getServerWorld()) {
            throw new IllegalArgumentException("CONNECTION_TELEPORT_REQUIRES_SAME_WORLD");
        }
        if (connection.player.isRemoved()) return;
        connection.requestTeleport(destination.getX(), destination.getY(), destination.getZ(),
                destination.getYaw(), destination.getPitch());
    }

    public static PaperEntityAccess state(Entity entity) {
        if (!(entity instanceof PaperEntityAccess access)) throw new IllegalStateException("ENTITY_EXTENSION_MIXIN_MISSING");
        return access;
    }
    public static boolean eventsAvailable() { Binding active = binding; return active != null && active.context.entitiesReady(); }
    public static void tracking(Entity entity, boolean tracked) { state(entity).meldrite$setTracked(tracked); }
    public static void requireThread(Entity entity) {
        if (!(entity.getWorld() instanceof ServerWorld world)) throw new IllegalStateException("ENTITY_NOT_IN_SERVER_WORLD");
        requireThread(world);
    }
    public static void requireThread(ServerWorld world) {
        if (!world.getServer().isOnThread()) throw new IllegalStateException("ASYNC_ENTITY_WRITE");
    }
    private static Binding requireBinding() {
        Binding active = binding;
        if (active == null) throw new IllegalStateException("ENTITY_FACADES_NOT_BOUND");
        return active;
    }
}
