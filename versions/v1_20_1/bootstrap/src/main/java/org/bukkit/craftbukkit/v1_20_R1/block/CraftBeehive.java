package org.bukkit.craftbukkit.v1_20_R1.block;

import java.util.List;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Location;
import org.bukkit.block.Beehive;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftBee;
import org.bukkit.entity.Bee;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.NativeBeehiveCapacity;
import dev.meldrite.v1201.hook.BeehiveBlockEntityAccessMixin;

/** Bukkit beehive state backed by native bee NBT and release logic. */
public final class CraftBeehive extends CraftBlockEntityState<BeehiveBlockEntity> implements Beehive {
    public CraftBeehive(ServerWorld world, BlockPos position, BlockState state, BeehiveBlockEntity tile, boolean useSnapshot) {
        super(world, position, state, tile, useSnapshot);
    }
    private BeehiveBlockEntityAccessMixin access() { return (BeehiveBlockEntityAccessMixin) (Object) snapshot; }
    private NativeBeehiveCapacity capacityAccess() { return (NativeBeehiveCapacity) (Object) snapshot; }

    @Override public Location getFlower() {
        BlockPos flower = access().meldrite$flower();
        return flower == null ? null : new Location(isPlaced() ? getWorld() : null, flower.getX(), flower.getY(), flower.getZ());
    }
    @Override public void setFlower(Location location) {
        if (location != null) {
            location.checkFinite();
            if (isPlaced() && location.getWorld() != getWorld()) throw new IllegalArgumentException("Flower must be in same world");
            access().meldrite$flower(BlockPos.ofFloored(location.getX(), location.getY(), location.getZ()));
        } else access().meldrite$flower(null);
    }
    @Override public boolean isFull() { return snapshot.isFullOfBees(); }
    @Override public boolean isSedated() { return isPlaced() && liveTile().isSmoked(); }
    @Override public int getEntityCount() { return snapshot.getBeeCount(); }
    @Override public int getMaxEntities() { return capacityAccess().meldrite$getMaxBees(); }
    @Override public void setMaxEntities(int max) { capacityAccess().meldrite$setMaxBees(max); }
    @Override public List<Bee> releaseEntities() {
        if (!isPlaced()) return List.of();
        requirePlaced();
        return capacityAccess().meldrite$releaseBees(world, state, true).stream()
                .filter(entity -> entity instanceof BeeEntity)
                .map(entity -> (Bee) NativeEntityExtensions.bukkit(entity)).toList();
    }
    @Override public void addEntity(Bee entity) {
        Objects.requireNonNull(entity, "entity");
        if (!(entity instanceof CraftBee craft)) throw new IllegalArgumentException("Bee must be a native CraftBee");
        if (isFull()) throw new IllegalStateException("Beehive is full");
        if (!craft.getHandle().getWorld().getServer().isOnThread()) throw new IllegalStateException("ASYNC_ENTITY_WRITE");
        snapshot.tryEnterHive(craft.getHandle(), false);
    }
    @Override public void clearEntities() {
        NbtCompound nbt = snapshot.createNbt();
        nbt.put("Bees", new NbtList());
        snapshot.readNbt(nbt);
    }
}
