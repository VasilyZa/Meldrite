package dev.meldrite.v1201.compat;

import java.util.HashMap;
import java.util.UUID;

/** Server-side Paper loot/refill state for native inventories. */
public final class VehicleLootState {
    private static final String KEY = "Paper.LootableData";
    private final HashMap<UUID, Long> lastLooted = new HashMap<>();
    private long lastFilled = -1L;
    private long nextRefill = -1L;
    private int refills;

    public boolean hasPlayerLooted(UUID player) { return player != null && lastLooted.containsKey(player); }
    public Long getLastLooted(UUID player) { return player == null ? null : lastLooted.get(player); }
    public boolean setHasPlayerLooted(UUID player, boolean looted) {
        if (player == null) return false;
        boolean previous = lastLooted.containsKey(player);
        if (looted) lastLooted.put(player, System.currentTimeMillis());
        else lastLooted.remove(player);
        return previous;
    }
    public boolean hasPendingRefill() { return nextRefill != -1L && nextRefill > lastFilled; }
    public long getLastFilled() { return lastFilled; }
    public long getNextRefill() { return nextRefill; }
    public long setNextRefill(long value) {
        if (value < -1L) value = -1L;
        long previous = nextRefill;
        nextRefill = value;
        return previous;
    }
    public void markFilled() { lastFilled = System.currentTimeMillis(); refills++; }

    public void write(net.minecraft.nbt.NbtCompound nbt) {
        var state = new net.minecraft.nbt.NbtCompound();
        if (lastFilled != -1L) state.putLong("lastFill", lastFilled);
        if (nextRefill != -1L) state.putLong("nextRefill", nextRefill);
        if (refills != 0) state.putInt("numRefills", refills);
        if (!lastLooted.isEmpty()) {
            var players = new net.minecraft.nbt.NbtList();
            lastLooted.forEach((uuid, time) -> {
                var player = new net.minecraft.nbt.NbtCompound();
                player.putUuid("UUID", uuid);
                player.putLong("Time", time);
                players.add(player);
            });
            state.put("lootedPlayers", players);
        }
        if (!state.isEmpty()) nbt.put(KEY, state);
    }
    public void read(net.minecraft.nbt.NbtCompound nbt) {
        var state = nbt.getCompound(KEY);
        lastFilled = state.contains("lastFill", net.minecraft.nbt.NbtElement.LONG_TYPE) ? state.getLong("lastFill") : -1L;
        nextRefill = state.contains("nextRefill", net.minecraft.nbt.NbtElement.LONG_TYPE) ? state.getLong("nextRefill") : -1L;
        refills = state.contains("numRefills", net.minecraft.nbt.NbtElement.INT_TYPE) ? state.getInt("numRefills") : 0;
        lastLooted.clear();
        if (state.contains("lootedPlayers", net.minecraft.nbt.NbtElement.LIST_TYPE)) {
            var players = state.getList("lootedPlayers", net.minecraft.nbt.NbtElement.COMPOUND_TYPE);
            for (int index = 0; index < players.size(); index++) {
                var player = players.getCompound(index);
                if (player.containsUuid("UUID")) lastLooted.put(player.getUuid("UUID"), player.getLong("Time"));
            }
        }
    }
}
