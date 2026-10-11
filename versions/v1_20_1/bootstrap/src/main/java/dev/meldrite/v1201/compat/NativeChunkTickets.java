package dev.meldrite.v1201.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.bukkit.plugin.Plugin;

/** Tracks Bukkit-owned tickets while the native ticket manager controls chunk residency. */
public final class NativeChunkTickets {
    private static final ChunkTicketType<ChunkPos> BUKKIT_LOAD = ChunkTicketType.create("meldrite_bukkit_load", Comparator.comparingLong(ChunkPos::toLong));
    private static final ChunkTicketType<Plugin> PLUGIN = ChunkTicketType.create("meldrite_plugin", Comparator.comparing(Plugin::getName).thenComparingInt(System::identityHashCode));
    private static final Map<ServerWorld, Map<Long, Set<Plugin>>> PLUGIN_TICKETS = new WeakHashMap<>();
    private static final Map<ServerWorld, Set<Long>> LOAD_TICKETS = new WeakHashMap<>();

    private NativeChunkTickets() { }

    public static synchronized boolean addPlugin(ServerWorld world, int x, int z, Plugin plugin) {
        java.util.Objects.requireNonNull(plugin, "plugin");
        long key = ChunkPos.toLong(x, z);
        Set<Plugin> owners = PLUGIN_TICKETS.computeIfAbsent(world, ignored -> new java.util.HashMap<>())
                .computeIfAbsent(key, ignored -> Collections.newSetFromMap(new IdentityHashMap<>()));
        if (!owners.add(plugin)) return false;
        world.getChunkManager().addTicket(PLUGIN, new ChunkPos(x, z), 31, plugin);
        return true;
    }

    public static synchronized boolean removePlugin(ServerWorld world, int x, int z, Plugin plugin) {
        Map<Long, Set<Plugin>> chunks = PLUGIN_TICKETS.get(world);
        if (chunks == null) return false;
        long key = ChunkPos.toLong(x, z);
        Set<Plugin> owners = chunks.get(key);
        if (owners == null || !owners.remove(plugin)) return false;
        world.getChunkManager().removeTicket(PLUGIN, new ChunkPos(x, z), 31, plugin);
        if (owners.isEmpty()) chunks.remove(key);
        if (chunks.isEmpty()) PLUGIN_TICKETS.remove(world);
        return true;
    }

    public static synchronized Set<Plugin> plugins(ServerWorld world, int x, int z) {
        Map<Long, Set<Plugin>> chunks = PLUGIN_TICKETS.get(world);
        Set<Plugin> owners = chunks == null ? null : chunks.get(ChunkPos.toLong(x, z));
        if (owners == null) return Set.of();
        Set<Plugin> copy = Collections.newSetFromMap(new IdentityHashMap<>());
        copy.addAll(owners);
        return Collections.unmodifiableSet(copy);
    }

    public static synchronized Map<Plugin, java.util.Collection<org.bukkit.Chunk>> allPlugins(ServerWorld world,
            java.util.function.BiFunction<Integer, Integer, org.bukkit.Chunk> chunkFactory) {
        Map<Plugin, java.util.Collection<org.bukkit.Chunk>> result = new IdentityHashMap<>();
        Map<Long, Set<Plugin>> chunks = PLUGIN_TICKETS.get(world);
        if (chunks == null) return result;
        chunks.forEach((key, owners) -> {
            int x = ChunkPos.getPackedX(key), z = ChunkPos.getPackedZ(key);
            for (Plugin plugin : owners) result.computeIfAbsent(plugin, ignored -> new ArrayList<>()).add(chunkFactory.apply(x, z));
        });
        return result;
    }

    public static synchronized boolean addLoad(ServerWorld world, int x, int z) {
        long key = ChunkPos.toLong(x, z);
        if (!LOAD_TICKETS.computeIfAbsent(world, ignored -> new java.util.HashSet<>()).add(key)) return false;
        world.getChunkManager().addTicket(BUKKIT_LOAD, new ChunkPos(x, z), 31, new ChunkPos(x, z));
        return true;
    }

    public static synchronized boolean removeLoad(ServerWorld world, int x, int z) {
        Set<Long> chunks = LOAD_TICKETS.get(world);
        long key = ChunkPos.toLong(x, z);
        if (chunks == null || !chunks.remove(key)) return false;
        var position = new ChunkPos(x, z);
        world.getChunkManager().removeTicket(BUKKIT_LOAD, position, 31, position);
        if (chunks.isEmpty()) LOAD_TICKETS.remove(world);
        return true;
    }
}
