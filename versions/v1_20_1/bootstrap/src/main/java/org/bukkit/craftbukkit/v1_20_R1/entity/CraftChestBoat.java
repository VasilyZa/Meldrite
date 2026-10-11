package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;

public final class CraftChestBoat extends CraftBoat implements org.bukkit.entity.ChestBoat {
    private final CraftInventory inventory;
    public CraftChestBoat(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); this.inventory = new CraftInventory((ChestBoatEntity) super.getHandle(), this); }
    @Override public ChestBoatEntity getHandle() { return (ChestBoatEntity) super.getHandle(); }
    @Override public void meldrite$rebindNative(net.minecraft.entity.Entity replacement) {
        if (!(replacement instanceof ChestBoatEntity next)) throw new IllegalArgumentException("CHEST_BOAT_REBIND_REQUIRES_CHEST_BOAT");
        super.meldrite$rebindNative(replacement);
        inventory.meldrite$rebindInventory(next);
    }
    @Override public org.bukkit.inventory.Inventory getInventory() { return inventory; }
    @Override public org.bukkit.entity.Entity getEntity() { return this; }
    @Override public boolean isRefillEnabled() { return getLootTable() != null; }
    @Override public boolean hasBeenFilled() { return getLootTable() == null; }
    @Override public boolean canPlayerLoot(java.util.UUID player) { return player != null && getLootTable() != null; }
    private dev.meldrite.v1201.compat.VehicleLootState lootState() { return ((dev.meldrite.v1201.compat.VehicleLootStateAccess) (Object) getHandle()).meldrite$lootState(); }
    @Override public boolean hasPlayerLooted(java.util.UUID player) { return lootState().hasPlayerLooted(player); }
    @Override public Long getLastLooted(java.util.UUID player) { return lootState().getLastLooted(player); }
    @Override public boolean setHasPlayerLooted(java.util.UUID player, boolean looted) { requireWritable(); return lootState().setHasPlayerLooted(player, looted); }
    @Override public boolean hasPendingRefill() { return lootState().hasPendingRefill(); }
    @Override public long getLastFilled() { return lootState().getLastFilled(); }
    @Override public long getNextRefill() { return lootState().getNextRefill(); }
    @Override public long setNextRefill(long refill) { requireWritable(); return lootState().setNextRefill(refill); }
    @Override public void setLootTable(org.bukkit.loot.LootTable table) { setLootTable(table, getSeed()); }
    @Override public org.bukkit.loot.LootTable getLootTable() { var id = getHandle().getLootTableId(); return id == null ? null : dev.meldrite.v1201.compat.NativeLootTables.get(getHandle().getWorld().getServer().getLootManager(), id); }
    @Override public void setSeed(long seed) { setLootTable(getLootTable(), seed); }
    @Override public long getSeed() { return getHandle().getLootTableSeed(); }
    public void setLootTable(org.bukkit.loot.LootTable table, long seed) { requireWritable(); getHandle().setLootTableId(table == null ? null : new net.minecraft.util.Identifier(table.getKey().getNamespace(), table.getKey().getKey())); getHandle().setLootTableSeed(seed); }
}
