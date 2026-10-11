package org.bukkit.craftbukkit.v1_20_R1.block;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import dev.meldrite.v1201.hook.MobSpawnerLogicAccessMixin;

/** Bukkit mob spawner state backed by the native MobSpawnerLogic fields. */
public final class CraftCreatureSpawner extends CraftBlockEntityState<MobSpawnerBlockEntity> implements CreatureSpawner {
    public CraftCreatureSpawner(ServerWorld world, BlockPos position, BlockState state, MobSpawnerBlockEntity tile, boolean useSnapshot) {
        super(world, position, state, tile, useSnapshot);
    }
    private MobSpawnerLogic logic() { return snapshot.getLogic(); }
    private MobSpawnerLogicAccessMixin access() { return (MobSpawnerLogicAccessMixin) (Object) logic(); }
    private static EntityType<?> nativeType(org.bukkit.entity.EntityType type) {
        if (type == null) return null;
        if (type == org.bukkit.entity.EntityType.UNKNOWN) throw new IllegalArgumentException("Cannot spawn UNKNOWN entity type");
        return EntityType.get(type.getKey().toString()).orElseThrow(() -> new IllegalArgumentException("Unknown native entity type: " + type));
    }
    private void setEntry(EntityType<?> type) {
        if (type == null) {
            access().meldrite$setSpawnEntry(isPlaced() ? world : null, position, new MobSpawnerEntry(new NbtCompound(), Optional.empty()));
        } else {
            logic().setEntityId(type, isPlaced() ? world : null, isPlaced() ? world.getRandom() : net.minecraft.util.math.random.Random.create(), position);
        }
        access().meldrite$renderedEntity(null);
        access().meldrite$spawnPotentials(DataPool.<MobSpawnerEntry>empty());
    }
    @Override public org.bukkit.entity.EntityType getSpawnedType() {
        MobSpawnerEntry entry = access().meldrite$spawnEntry();
        if (entry == null || !entry.entity().contains("id")) return null;
        var id = net.minecraft.util.Identifier.tryParse(entry.entity().getString("id"));
        return id == null ? null : org.bukkit.Registry.ENTITY_TYPE.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
    }
    @Override public void setSpawnedType(org.bukkit.entity.EntityType type) { setEntry(nativeType(type)); }
    @Override public void setCreatureTypeByName(String name) {
        org.bukkit.entity.EntityType type = name == null ? null : org.bukkit.entity.EntityType.fromName(name);
        setSpawnedType(type);
    }
    @Override public String getCreatureTypeName() {
        var type = getSpawnedType(); return type == null ? null : type.getName();
    }
    @Override public int getDelay() { return access().meldrite$spawnDelay(); }
    @Override public void setDelay(int delay) { access().meldrite$spawnDelay(delay); }
    @Override public int getMinSpawnDelay() { return access().meldrite$minSpawnDelay(); }
    @Override public void setMinSpawnDelay(int delay) {
        if (delay > getMaxSpawnDelay()) throw new IllegalArgumentException("Minimum Spawn Delay must be less than or equal to Maximum Spawn Delay");
        access().meldrite$minSpawnDelay(delay);
    }
    @Override public int getMaxSpawnDelay() { return access().meldrite$maxSpawnDelay(); }
    @Override public void setMaxSpawnDelay(int delay) {
        if (delay <= 0 || delay < getMinSpawnDelay()) throw new IllegalArgumentException("Maximum Spawn Delay must be positive and >= minimum");
        access().meldrite$maxSpawnDelay(delay);
    }
    @Override public int getSpawnCount() { return access().meldrite$spawnCount(); }
    @Override public void setSpawnCount(int count) { access().meldrite$spawnCount(count); }
    @Override public int getMaxNearbyEntities() { return access().meldrite$maxNearbyEntities(); }
    @Override public void setMaxNearbyEntities(int value) { access().meldrite$maxNearbyEntities(value); }
    @Override public int getRequiredPlayerRange() { return access().meldrite$requiredPlayerRange(); }
    @Override public void setRequiredPlayerRange(int value) { access().meldrite$requiredPlayerRange(value); }
    @Override public int getSpawnRange() { return access().meldrite$spawnRange(); }
    @Override public void setSpawnRange(int value) { access().meldrite$spawnRange(value); }
    @Override public boolean isActivated() {
        requirePlaced();
        return access().meldrite$isPlayerInRange(world, position);
    }
    @Override public void resetTimer() { requirePlaced(); access().meldrite$updateSpawns(world, position); }
    @Override public void setSpawnedItem(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) throw new IllegalArgumentException("spawners cannot spawn air");
        ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:item");
        entity.put("Item", item.writeNbt(new NbtCompound()));
        access().meldrite$setSpawnEntry(isPlaced() ? world : null, position, new MobSpawnerEntry(entity, Optional.empty()));
        access().meldrite$renderedEntity(null);
    }
}
