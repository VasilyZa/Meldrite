package io.papermc.paper.chunk.system.entity;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;

/** Paper lookup ABI backed by the world's native index and insertion lifecycle. */
public final class EntityLookup implements net.minecraft.world.entity.EntityLookup<Entity> {
    private final ServerWorld world;
    private final net.minecraft.world.entity.EntityLookup<Entity> nativeLookup;
    public EntityLookup(ServerWorld world, net.minecraft.world.entity.EntityLookup<Entity> nativeLookup) {
        this.world = Objects.requireNonNull(world);
        this.nativeLookup = Objects.requireNonNull(nativeLookup);
    }
    public boolean addNewEntity(Entity entity) {
        NativeEntityExtensions.requireThread(world);
        Objects.requireNonNull(entity);
        if (entity.getWorld() != world || entity.isRemoved()) return false;
        return world.spawnEntity(entity);
    }
    @Override public Entity get(int id) { return nativeLookup.get(id); }
    @Override public Entity get(UUID uuid) { return nativeLookup.get(uuid); }
    @Override public Iterable<Entity> iterate() { return nativeLookup.iterate(); }
    @Override public <U extends Entity> void forEach(TypeFilter<Entity, U> filter, LazyIterationConsumer<U> consumer) {
        nativeLookup.forEach(filter, consumer);
    }
    @Override public void forEachIntersects(Box box, Consumer<Entity> consumer) {
        nativeLookup.forEachIntersects(box, consumer);
    }
    @Override public <U extends Entity> void forEachIntersects(TypeFilter<Entity, U> filter, Box box, LazyIterationConsumer<U> consumer) {
        nativeLookup.forEachIntersects(filter, box, consumer);
    }
}
