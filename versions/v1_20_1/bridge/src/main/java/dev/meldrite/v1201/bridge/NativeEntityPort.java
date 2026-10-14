package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.EntityPort;
import dev.meldrite.foundation.EntityHandle;
import dev.meldrite.foundation.EntityPosition;
import dev.meldrite.foundation.EntitySpawnReceipt;
import dev.meldrite.foundation.ResourceKey;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Optional;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Native entity projection used by the event ingress; player identity is UUID based. */
public final class NativeEntityPort implements EntityPort {
    private final MinecraftServer server;
    private final NativeWorldPort worlds;
    private final ConcurrentMap<EntityHandle, Projection> projected = new ConcurrentHashMap<>();
    private final ReferenceQueue<Entity> collected = new ReferenceQueue<>();
    private static final class Projection extends WeakReference<Entity> {
        final EntityHandle handle;
        Projection(EntityHandle handle, Entity entity, ReferenceQueue<Entity> collected) {
            super(entity, collected);
            this.handle = handle;
        }
    }

    public NativeEntityPort(MinecraftServer server, NativeWorldPort worlds) {
        this.server = Objects.requireNonNull(server);
        this.worlds = Objects.requireNonNull(worlds);
    }

    @Override public boolean isValid(EntityHandle handle) {
        Entity entity = resolve(handle);
        return entity != null && !entity.isRemoved() && matchesGeneration(handle, entity);
    }

    @Override public Optional<EntityInfo> describe(EntityHandle handle) {
        if (!worlds.isLoaded(handle.world())) return Optional.empty();
        Entity entity = resolve(handle);
        if (entity == null || entity.isRemoved() || !matchesGeneration(handle, entity)) return Optional.empty();
        EntityPosition position = new EntityPosition(handle.world(), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        Identifier typeId = Registries.ENTITY_TYPE.getId(entity.getType());
        ResourceKey type = new ResourceKey("minecraft", "entity_type", typeId.getNamespace(), typeId.getPath());
        return Optional.of(new EntityInfo(handle, position, type, entity instanceof ServerPlayerEntity, entity.getName().getString()));
    }

    public ServerPlayerEntity player(EntityHandle handle) {
        Entity entity = require(handle);
        if (!(entity instanceof ServerPlayerEntity player)) {
            throw new IllegalStateException("ENTITY_NOT_PRESENT: " + handle.uuid());
        }
        return player;
    }
    public Entity require(EntityHandle handle) {
        Entity entity = resolve(handle);
        if (entity == null || entity.isRemoved() || !matchesGeneration(handle, entity)) throw new IllegalStateException("ENTITY_NOT_PRESENT: " + handle.uuid());
        return entity;
    }

    /** Retains native identity without inserting a detached entity into its world. */
    public EntityHandle project(Entity entity) {
        requirePrimary();
        if (!(entity.getWorld() instanceof net.minecraft.server.world.ServerWorld world)
                || !worlds.isLoaded(NativeWorldPort.key(world))
                || worlds.require(NativeWorldPort.key(world)) != world || entity.isRemoved()) {
            throw new IllegalStateException("ENTITY_NOT_IN_LIVE_SERVER_WORLD");
        }
        var handle = new EntityHandle(NativeWorldPort.key(world), entity.getUuid(), (long) entity.getId() + 1);
        Projection previous;
        while ((previous = (Projection) collected.poll()) != null) projected.remove(previous.handle, previous);
        projected.put(handle, new Projection(handle, entity, collected));
        return handle;
    }

    private Entity resolve(EntityHandle handle) {
        if (!worlds.isLoaded(handle.world())) return null;
        var world = worlds.require(handle.world());
        WeakReference<Entity> reference = projected.get(handle);
        Entity entity = reference == null ? null : reference.get();
        if (entity != null && !entity.isRemoved() && entity.getWorld() == world && matchesGeneration(handle, entity)) return entity;
        entity = world.getEntity(handle.uuid());
        return entity != null && !entity.isRemoved() && matchesGeneration(handle, entity) ? entity : null;
    }

    @Override public void remove(EntityHandle handle) { requirePrimary(); require(handle).discard(); }

    @Override public EntityPosition position(EntityHandle handle) {
        Entity entity = require(handle);
        return new EntityPosition(handle.world(), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
    }

    @Override public ResourceKey type(EntityHandle handle) {
        Identifier id = Registries.ENTITY_TYPE.getId(require(handle).getType());
        return new ResourceKey("minecraft", "entity_type", id.getNamespace(), id.getPath());
    }

    @Override public boolean teleport(EntityHandle handle, EntityPosition position) {
        requirePrimary();
        Entity entity = require(handle);
        if (!position.world().equals(handle.world())) throw new UnsupportedOperationException("CROSS_WORLD_TELEPORT_REQUIRES_REBIND");
        if (entity instanceof ServerPlayerEntity player) {
            player.networkHandler.requestTeleport(position.x(), position.y(), position.z(), position.yaw(), position.pitch());
        } else {
            entity.refreshPositionAndAngles(position.x(), position.y(), position.z(), position.yaw(), position.pitch());
        }
        return true;
    }

    @Override public void sendMessage(EntityHandle handle, String message) {
        requirePrimary();
        player(handle).sendMessage(net.minecraft.text.Text.literal(message));
    }

    @Override public EntitySpawnReceipt spawn(ResourceKey type, EntityPosition position) {
        requirePrimary();
        Identifier id = new Identifier(type.entryNamespace(), type.entryPath());
        if (!Registries.ENTITY_TYPE.containsId(id)) throw new IllegalArgumentException("UNKNOWN_ENTITY_TYPE: " + id);
        var world = worlds.require(position.world());
        Entity entity = Registries.ENTITY_TYPE.get(id).create(world);
        if (entity == null || entity instanceof ServerPlayerEntity) throw new IllegalArgumentException("UNSPAWNABLE_ENTITY_TYPE: " + id);
        entity.refreshPositionAndAngles(position.x(), position.y(), position.z(), position.yaw(), position.pitch());
        if (!world.spawnEntity(entity)) throw new IllegalStateException("NATIVE_ENTITY_SPAWN_REJECTED: " + id);
        return new EntitySpawnReceipt(new EntityHandle(position.world(), entity.getUuid(), (long) entity.getId() + 1), entity.getId(), entity.getName().getString());
    }

    private void requirePrimary() {
        if (!server.isOnThread()) throw new IllegalStateException("ASYNC_ENTITY_WRITE");
    }
    private static boolean matchesGeneration(EntityHandle handle, Entity entity) {
        return handle.generation() == 0 || handle.generation() == (long) entity.getId() + 1;
    }
}
