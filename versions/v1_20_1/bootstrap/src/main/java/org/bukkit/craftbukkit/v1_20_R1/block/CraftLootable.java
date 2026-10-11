package org.bukkit.craftbukkit.v1_20_R1.block;

import dev.meldrite.v1201.compat.VehicleLootState;
import dev.meldrite.v1201.compat.VehicleLootStateAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.loot.LootTable;

public abstract class CraftLootable<T extends LootableContainerBlockEntity> extends CraftContainer
        implements com.destroystokyo.paper.loottable.LootableBlockInventory {
    protected CraftLootable(ServerWorld world, BlockPos pos, BlockState state, T tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @SuppressWarnings("unchecked") @Override public T getTileEntity() { return (T) super.getTileEntity(); }
    @SuppressWarnings("unchecked") @Override public T getSnapshot() { return (T) super.getSnapshot(); }
    @SuppressWarnings("unchecked") @Override protected T liveTile() { return (T) super.liveTile(); }
    private VehicleLootState lootState() { return ((VehicleLootStateAccess) liveTile()).meldrite$lootState(); }
    @Override public LootTable getLootTable() {
        var id = getSnapshot().lootTableId;
        return id == null ? null : dev.meldrite.v1201.compat.NativeLootTables.get(
                ((org.bukkit.craftbukkit.v1_20_R1.CraftServer) org.bukkit.Bukkit.getServer()).getServer().getLootManager(), id);
    }
    @Override public void setLootTable(LootTable table) { setLootTable(table, getSeed()); }
    public void setLootTable(LootTable table, long seed) {
        getSnapshot().setLootTable(table == null ? null : org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(table.getKey()), seed);
    }
    @Override public long getSeed() { return getSnapshot().lootTableSeed; }
    @Override public void setSeed(long seed) { setLootTable(getLootTable(), seed); }
    @Override public boolean isRefillEnabled() { return false; }
    @Override public boolean hasBeenFilled() { return lootState().getLastFilled() != -1; }
    @Override public boolean canPlayerLoot(java.util.UUID player) {
        // Paper's default configuration permits a player to loot unless a
        // refill restriction is enabled; an empty table is not itself a ban.
        return player != null;
    }
    @Override public boolean hasPlayerLooted(java.util.UUID player) { return lootState().hasPlayerLooted(player); }
    @Override public Long getLastLooted(java.util.UUID player) { return lootState().getLastLooted(player); }
    @Override public boolean setHasPlayerLooted(java.util.UUID player, boolean looted) {
        var data = lootState();
        boolean previous = data.hasPlayerLooted(player);
        if (previous != looted) data.setHasPlayerLooted(player, looted);
        return previous;
    }
    @Override public boolean hasPendingRefill() { return lootState().hasPendingRefill(); }
    @Override public long getLastFilled() { return lootState().getLastFilled(); }
    @Override public long getNextRefill() { return lootState().getNextRefill(); }
    @Override public long setNextRefill(long value) { return lootState().setNextRefill(value); }
    @Override public boolean update(boolean force, boolean physics) {
        if (!isPlaced()) return super.update(force, physics);
        if (isSnapshot() && world.getBlockEntity(position) instanceof LootableContainerBlockEntity live
                && live.getClass() == tileEntity.getClass()) {
            var data = new net.minecraft.nbt.NbtCompound();
            lootState().write(data);
            ((VehicleLootStateAccess) getSnapshot()).meldrite$lootState().read(data);
        }
        if (!super.update(force, physics)) return false;
        if (world.getBlockEntity(position) instanceof LootableContainerBlockEntity live && live.getClass() == tileEntity.getClass()) {
            live.setLootTable(getSnapshot().lootTableId, getSeed());
        }
        return true;
    }
}
