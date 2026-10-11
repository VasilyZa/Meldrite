package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
public abstract class CraftMinecartContainer extends CraftMinecart implements org.bukkit.loot.Lootable {
    public CraftMinecartContainer(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public StorageMinecartEntity getHandle() { return (StorageMinecartEntity) super.getHandle(); }
    @Override public void setLootTable(org.bukkit.loot.LootTable table) { setLootTable(table, getSeed()); }
    @Override public org.bukkit.loot.LootTable getLootTable() { var id = getHandle().getLootTableId(); return id == null ? null : dev.meldrite.v1201.compat.NativeLootTables.get(getHandle().getWorld().getServer().getLootManager(), id); }
    @Override public void setSeed(long seed) { setLootTable(getLootTable(), seed); }
    @Override public long getSeed() { return getHandle().getLootTableSeed(); }
    public void setLootTable(org.bukkit.loot.LootTable table, long seed) { requireWritable(); getHandle().setLootTable(table == null ? null : new net.minecraft.util.Identifier(table.getKey().getNamespace(), table.getKey().getKey()), seed); }
}
