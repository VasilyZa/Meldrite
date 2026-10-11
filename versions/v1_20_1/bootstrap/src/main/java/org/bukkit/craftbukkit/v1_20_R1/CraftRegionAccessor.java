package org.bukkit.craftbukkit.v1_20_R1;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.RegionAccessor;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;

/** Shared RegionAccessor behavior for a live CraftWorld and generator regions. */
public abstract class CraftRegionAccessor implements RegionAccessor {
    public abstract World getWorld();
    protected final World world() { return Objects.requireNonNull(getWorld(), "world"); }
    @Override public Biome getBiome(Location location) { return world().getBiome(location); }
    @Override public Biome getBiome(int x, int y, int z) { return world().getBiome(x, y, z); }
    @Override public Biome getComputedBiome(int x, int y, int z) { return world().getComputedBiome(x, y, z); }
    @Override public void setBiome(Location location, Biome biome) { world().setBiome(location, biome); }
    @Override public void setBiome(int x, int y, int z, Biome biome) { world().setBiome(x, y, z, biome); }
    @Override public BlockState getBlockState(Location location) { return world().getBlockState(location); }
    @Override public BlockState getBlockState(int x, int y, int z) { return world().getBlockState(x, y, z); }
    @Override public BlockData getBlockData(Location location) { return world().getBlockData(location); }
    @Override public BlockData getBlockData(int x, int y, int z) { return world().getBlockData(x, y, z); }
    @Override public Material getType(Location location) { return world().getType(location); }
    @Override public Material getType(int x, int y, int z) { return world().getType(x, y, z); }
    @Override public void setBlockData(Location location, BlockData data) { world().setBlockData(location, data); }
    @Override public void setBlockData(int x, int y, int z, BlockData data) { world().setBlockData(x, y, z, data); }
    @Override public void setType(Location location, Material material) { world().setType(location, material); }
    @Override public void setType(int x, int y, int z, Material material) { world().setType(x, y, z, material); }
    @Override public boolean generateTree(Location location, Random random, TreeType type) { return world().generateTree(location, random, type); }
    @Override public boolean generateTree(Location location, Random random, TreeType type, Consumer<BlockState> consumer) { return world().generateTree(location, random, type, consumer); }
    @Override public boolean generateTree(Location location, Random random, TreeType type, Predicate<BlockState> predicate) { return world().generateTree(location, random, type, predicate); }
    @Override public Entity spawnEntity(Location location, EntityType type) { return world().spawnEntity(location, type); }
    @Override public Entity spawnEntity(Location location, EntityType type, boolean randomizeData) { return world().spawnEntity(location, type, randomizeData); }
    @Override public List<Entity> getEntities() { return world().getEntities(); }
    @Override public List<LivingEntity> getLivingEntities() { return world().getLivingEntities(); }
    @Override public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> type) { return world().getEntitiesByClass(type); }
    @Override public Collection<Entity> getEntitiesByClasses(Class<?>... types) { return world().getEntitiesByClasses(types); }
    @Override public <T extends Entity> T spawn(Location location, Class<T> type) { return world().spawn(location, type); }
    @Override public <T extends Entity> T spawn(Location location, Class<T> type, Consumer<T> consumer, CreatureSpawnEvent.SpawnReason reason) { return world().spawn(location, type, consumer, reason); }
    @Override public <T extends Entity> T spawn(Location location, Class<T> type, boolean randomizeData, Consumer<T> consumer) { return world().spawn(location, type, randomizeData, consumer); }
    @Override public int getHighestBlockYAt(int x, int z) { return world().getHighestBlockYAt(x, z); }
    @Override public int getHighestBlockYAt(Location location) { return world().getHighestBlockYAt(location); }
    @Override public int getHighestBlockYAt(int x, int z, org.bukkit.HeightMap map) { return world().getHighestBlockYAt(x, z, map); }
    @Override public int getHighestBlockYAt(Location location, org.bukkit.HeightMap map) { return world().getHighestBlockYAt(location, map); }
    @Override public io.papermc.paper.world.MoonPhase getMoonPhase() { return world().getMoonPhase(); }
    @Override public NamespacedKey getKey() { return world().getKey(); }
    @Override public boolean lineOfSightExists(Location from, Location to) { return world().lineOfSightExists(from, to); }
    @Override public boolean hasCollisionsIn(BoundingBox box) { return world().hasCollisionsIn(box); }
}
