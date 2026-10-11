package dev.meldrite.v1201.hook;

import net.minecraft.entity.EntityType;
import net.minecraft.util.collection.DataPool;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobSpawnerLogic.class)
public interface MobSpawnerLogicAccessMixin {
    @Accessor("spawnDelay") int meldrite$spawnDelay();
    @Accessor("spawnDelay") void meldrite$spawnDelay(int value);
    @Accessor("minSpawnDelay") int meldrite$minSpawnDelay();
    @Accessor("minSpawnDelay") void meldrite$minSpawnDelay(int value);
    @Accessor("maxSpawnDelay") int meldrite$maxSpawnDelay();
    @Accessor("maxSpawnDelay") void meldrite$maxSpawnDelay(int value);
    @Accessor("spawnCount") int meldrite$spawnCount();
    @Accessor("spawnCount") void meldrite$spawnCount(int value);
    @Accessor("maxNearbyEntities") int meldrite$maxNearbyEntities();
    @Accessor("maxNearbyEntities") void meldrite$maxNearbyEntities(int value);
    @Accessor("requiredPlayerRange") int meldrite$requiredPlayerRange();
    @Accessor("requiredPlayerRange") void meldrite$requiredPlayerRange(int value);
    @Accessor("spawnRange") int meldrite$spawnRange();
    @Accessor("spawnRange") void meldrite$spawnRange(int value);
    @Accessor("spawnEntry") MobSpawnerEntry meldrite$spawnEntry();
    @Accessor("spawnEntry") void meldrite$spawnEntry(MobSpawnerEntry value);
    @Accessor("spawnPotentials") DataPool<MobSpawnerEntry> meldrite$spawnPotentials();
    @Accessor("spawnPotentials") void meldrite$spawnPotentials(DataPool<MobSpawnerEntry> value);
    @Accessor("renderedEntity") void meldrite$renderedEntity(net.minecraft.entity.Entity value);
    @Invoker("isPlayerInRange") boolean meldrite$isPlayerInRange(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos);
    @Invoker("updateSpawns") void meldrite$updateSpawns(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos);
    @Invoker("setSpawnEntry") void meldrite$setSpawnEntry(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, MobSpawnerEntry entry);
}
