package dev.meldrite.v1201.compat;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.world.ServerWorld;
import org.bukkit.entity.SpawnCategory;

/** Runtime state for Bukkit's per-world settings that 1.20.1 does not persist natively. */
public final class NativeWorldSettings {
    private static final Map<ServerWorld, State> STATES = new WeakHashMap<>();

    private NativeWorldSettings() { }

    private static State state(ServerWorld world) {
        return STATES.computeIfAbsent(world, ignored -> new State());
    }

    public static synchronized int getSpawnLimit(ServerWorld world, SpawnCategory category) {
        valid(category);
        return state(world).limits.getOrDefault(category, defaultLimit(category));
    }

    public static synchronized void setSpawnLimit(ServerWorld world, SpawnCategory category, int limit) {
        valid(category);
        if (limit < -1) throw new IllegalArgumentException("limit must be >= -1");
        state(world).limits.put(category, limit);
    }

    public static synchronized long getTicksPerSpawns(ServerWorld world, SpawnCategory category) {
        valid(category);
        return state(world).ticks.getOrDefault(category,  tickDefault(category));
    }

    public static synchronized void setTicksPerSpawns(ServerWorld world, SpawnCategory category, int ticks) {
        valid(category);
        if (ticks < 0) throw new IllegalArgumentException("ticks must be >= 0");
        state(world).ticks.put(category, (long) ticks);
    }

    public static synchronized boolean getKeepSpawnInMemory(ServerWorld world) {
        return state(world).keepSpawnInMemory;
    }

    public static synchronized void setKeepSpawnInMemory(ServerWorld world, boolean keep) {
        State state = state(world);
        if (state.keepSpawnInMemory == keep) return;
        state.keepSpawnInMemory = keep;
        var pos = world.getSpawnPos();
        if (keep) NativeChunkTickets.addLoad(world, pos.getX() >> 4, pos.getZ() >> 4);
        else NativeChunkTickets.removeLoad(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static void valid(SpawnCategory category) {
        if (category == null || category == SpawnCategory.MISC) {
            throw new IllegalArgumentException("SpawnCategory is not valid for world spawn settings: " + category);
        }
    }

    private static int defaultLimit(SpawnCategory category) {
        return switch (category) {
            case MONSTER -> 70;
            case ANIMAL -> 10;
            case WATER_ANIMAL -> 5;
            case WATER_AMBIENT -> 20;
            case WATER_UNDERGROUND_CREATURE -> 5;
            case AMBIENT -> 15;
            default -> throw new IllegalArgumentException("Unsupported SpawnCategory: " + category);
        };
    }

    private static long tickDefault(SpawnCategory category) {
        return 1L;
    }

    private static final class State {
        private final EnumMap<SpawnCategory, Integer> limits = new EnumMap<>(SpawnCategory.class);
        private final EnumMap<SpawnCategory, Long> ticks = new EnumMap<>(SpawnCategory.class);
        private boolean keepSpawnInMemory = true;
    }
}
