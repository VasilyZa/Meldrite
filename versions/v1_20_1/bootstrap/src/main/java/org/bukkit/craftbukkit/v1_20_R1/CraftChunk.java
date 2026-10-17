package org.bukkit.craftbukkit.v1_20_R1;

import dev.meldrite.v1201.compat.NativeChunkTickets;
import dev.meldrite.v1201.compat.NativeChunkPersistentDataAccess;
import dev.meldrite.v1201.compat.WorldBukkitAccess;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.bukkit.Chunk;
import org.bukkit.Chunk.LoadLevel;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class CraftChunk implements Chunk {
    private static final CraftPersistentDataTypeRegistry DATA_TYPES = new CraftPersistentDataTypeRegistry();
    private final CraftWorld world;
    private volatile ServerWorld worldServer;
    private final int x, z;

    public CraftChunk(CraftWorld world, int x, int z) {
        this.world = Objects.requireNonNull(world);
        this.worldServer = world.getHandle();
        this.x = x;
        this.z = z;
    }

    public CraftChunk(WorldChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        if (!(chunk.getWorld() instanceof ServerWorld nativeWorld)) {
            throw new IllegalStateException("CRAFT_CHUNK_REQUIRES_SERVER_WORLD");
        }
        if (!(nativeWorld instanceof WorldBukkitAccess worldAccess)) {
            throw new IllegalStateException("WORLD_FACADE_NOT_PRESENT");
        }
        CraftWorld craftWorld = worldAccess.meldrite$getBukkitWorld();
        if (craftWorld == null) throw new IllegalStateException("WORLD_FACADE_NOT_PRESENT");
        if (craftWorld.getHandle() != nativeWorld) throw new IllegalStateException("CRAFT_CHUNK_WORLD_MISMATCH");

        var position = Objects.requireNonNull(chunk.getPos(), "chunk.pos");
        this.world = craftWorld;
        this.worldServer = nativeWorld;
        this.x = position.x;
        this.z = position.z;
    }

    private ServerWorld requireWorldServer() {
        ServerWorld current = worldServer;
        if (current == null) throw new IllegalStateException("STALE_CRAFT_CHUNK");

        ServerWorld canonical;
        try {
            canonical = world.getHandle();
        } catch (IllegalStateException invalidWorld) {
            if ("STALE_CRAFT_WORLD".equals(invalidWorld.getMessage())) {
                throw new IllegalStateException("STALE_CRAFT_CHUNK", invalidWorld);
            }
            if ("UNSUPPORTED_CRAFT_WORLD_REBIND".equals(invalidWorld.getMessage())) {
                throw new IllegalStateException("UNSUPPORTED_CRAFT_CHUNK_REBIND", invalidWorld);
            }
            throw invalidWorld;
        }
        if (canonical != current) throw new IllegalStateException("UNSUPPORTED_CRAFT_CHUNK_REBIND");
        return current;
    }

    public WorldChunk getHandle() {
        var chunk = requireWorldServer().getChunkManager().getChunk(x, z, ChunkStatus.FULL, false);
        if (!(chunk instanceof WorldChunk loaded)) throw new IllegalStateException("CHUNK_NOT_LOADED: " + x + "," + z);
        return loaded;
    }
    /** Returns this chunk at a specific native generation status. */
    public net.minecraft.world.chunk.Chunk getHandle(ChunkStatus status) {
        return requireWorldServer().getChunkManager().getChunk(x, z, Objects.requireNonNull(status, "status"), true);
    }
    @Override public int getX() { return x; }
    @Override public int getZ() { return z; }
    @Override public CraftWorld getWorld() { requireWorldServer(); return world; }
    @Override public String toString() { return "CraftChunk{" + x + "," + z + "}"; }
    @Override public Block getBlock(int blockX, int blockY, int blockZ) {
        if (blockX < 0 || blockX > 15 || blockZ < 0 || blockZ > 15) throw new IllegalArgumentException("Chunk-local X/Z must be in [0, 15]");
        ServerWorld current = requireWorldServer();
        if (blockY < current.getBottomY() || blockY >= current.getTopY()) throw new IllegalArgumentException("Y outside world height: " + blockY);
        return world.getBlockAt((x << 4) + blockX, blockY, (z << 4) + blockZ);
    }
    @Override public org.bukkit.ChunkSnapshot getChunkSnapshot() { return getChunkSnapshot(true, false, false); }
    @Override public org.bukkit.ChunkSnapshot getChunkSnapshot(boolean includeMaxBlockY, boolean includeBiome, boolean includeBiomeTemp) {
        return new CraftChunkSnapshot(world, getHandle(), includeBiome, includeBiomeTemp);
    }
    @Override public boolean isEntitiesLoaded() { return requireWorldServer().getChunkManager().isChunkLoaded(x, z); }
    @Override public Entity[] getEntities() {
        ServerWorld current = requireWorldServer();
        var result = new ArrayList<Entity>();
        for (var nativeEntity : current.iterateEntities()) {
            var position = nativeEntity.getChunkPos();
            if (position.x == x && position.z == z && !nativeEntity.isRemoved()) result.add(dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(nativeEntity));
        }
        return result.toArray(Entity[]::new);
    }
    @Override public BlockState[] getTileEntities(boolean useSnapshot) {
        return getTileEntities(block -> true, useSnapshot).toArray(BlockState[]::new);
    }
    @Override public Collection<BlockState> getTileEntities(Predicate<Block> predicate, boolean useSnapshot) {
        Objects.requireNonNull(predicate, "predicate");
        var chunk = getHandle();
        List<BlockState> result = new ArrayList<>();
        for (BlockPos position : chunk.getBlockEntities().keySet()) {
            Block block = world.getBlockAt(position.getX(), position.getY(), position.getZ());
            if (predicate.test(block)) result.add(block.getState(useSnapshot));
        }
        return result;
    }
    @Override public boolean isGenerated() { return requireWorldServer().getChunkManager().getChunk(x, z, ChunkStatus.FULL, false) instanceof WorldChunk; }
    @Override public boolean isLoaded() { return requireWorldServer().getChunkManager().isChunkLoaded(x, z); }
    @Override public boolean load(boolean generate) { requireWorldServer(); return world.loadChunk(x, z, generate); }
    @Override public boolean load() { return load(true); }
    @Override public boolean unload(boolean save) { requireWorldServer(); return world.unloadChunk(x, z, save); }
    @Override public boolean unload() { return unload(true); }
    @Override public boolean isSlimeChunk() {
        var random = net.minecraft.util.math.random.ChunkRandom.getSlimeRandom(x, z, requireWorldServer().getSeed(), 987234911L);
        return random.nextInt(10) == 0;
    }
    @Override public boolean isForceLoaded() { return requireWorldServer().getForcedChunks().contains(net.minecraft.util.math.ChunkPos.toLong(x, z)); }
    @Override public void setForceLoaded(boolean forced) { requireWorldServer().setChunkForced(x, z, forced); }
    @Override public boolean addPluginChunkTicket(Plugin plugin) { return NativeChunkTickets.addPlugin(requireWorldServer(), x, z, plugin); }
    @Override public boolean removePluginChunkTicket(Plugin plugin) { return NativeChunkTickets.removePlugin(requireWorldServer(), x, z, plugin); }
    @Override public Collection<Plugin> getPluginChunkTickets() { return NativeChunkTickets.plugins(requireWorldServer(), x, z); }
    @Override public long getInhabitedTime() { return getHandle().getInhabitedTime(); }
    @Override public void setInhabitedTime(long time) { dev.meldrite.v1201.compat.NativeEntityExtensions.requireThread(requireWorldServer()); getHandle().setInhabitedTime(time); }
    @Override public boolean contains(BlockData data) {
        var nativeData = dev.meldrite.v1201.compat.NativeBlockData.require(Objects.requireNonNull(data));
        for (var section : getHandle().getSectionArray()) if (section.hasAny(state -> state == nativeData)) return true;
        return false;
    }
    @Override public boolean contains(Biome biome) {
        Objects.requireNonNull(biome);
        var chunk = getHandle();
        for (int section = 0; section < chunk.getSectionArray().length; section++) {
            var values = chunk.getSection(section).getBiomeContainer();
            for (int by = 0; by < 4; by++) for (int bz = 0; bz < 4; bz++) for (int bx = 0; bx < 4; bx++) {
                var key = values.get(bx, by, bz).getKey().orElse(null);
                if (key == null) continue;
                var projected = org.bukkit.Registry.BIOME.get(org.bukkit.NamespacedKey.fromString(key.getValue().toString()));
                if (biome == projected) return true;
            }
        }
        return false;
    }
    @Override public LoadLevel getLoadLevel() {
        if (!isLoaded()) return LoadLevel.UNLOADED;
        return switch (getHandle().getLevelType()) {
            case ENTITY_TICKING -> LoadLevel.ENTITY_TICKING;
            case BLOCK_TICKING -> LoadLevel.TICKING;
            case FULL -> LoadLevel.BORDER;
            case INACCESSIBLE -> LoadLevel.INACCESSIBLE;
        };
    }
    @Override public CraftPersistentDataContainer getPersistentDataContainer() {
        return ((NativeChunkPersistentDataAccess) getHandle()).meldrite$getPersistentDataContainer();
    }
}
