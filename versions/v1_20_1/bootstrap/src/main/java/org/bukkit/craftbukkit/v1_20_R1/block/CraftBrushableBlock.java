package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.loot.LootTable;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;

public class CraftBrushableBlock extends CraftBlockEntityState<BrushableBlockEntity> implements org.bukkit.block.BrushableBlock {
    public CraftBrushableBlock(ServerWorld world, BlockPos pos, BlockState state, BrushableBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public org.bukkit.inventory.ItemStack getItem() { return CraftItemStack.asBukkitCopy(snapshot.getItem()); }
    @Override public void setItem(org.bukkit.inventory.ItemStack item) { snapshot.item = CraftItemStack.asNMSCopy(item); }
    @Override public LootTable getLootTable() { return snapshot.lootTable == null ? null : dev.meldrite.v1201.compat.NativeLootTables.get(world.getServer().getLootManager(), snapshot.lootTable); }
    @Override public void setLootTable(LootTable table) { setLootTable(table, getSeed()); }
    public void setLootTable(LootTable table, long seed) { snapshot.setLootTable(table == null ? null : org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(table.getKey()), seed); }
    @Override public long getSeed() { return snapshot.lootTableSeed; }
    @Override public void setSeed(long seed) { setLootTable(getLootTable(), seed); }
    @Override public boolean update(boolean force, boolean physics) {
        if (!super.update(force, physics)) return false;
        if (world.getBlockEntity(position) instanceof BrushableBlockEntity live) { live.setLootTable(snapshot.lootTable, getSeed()); live.item = snapshot.item.copy(); }
        return true;
    }
}
