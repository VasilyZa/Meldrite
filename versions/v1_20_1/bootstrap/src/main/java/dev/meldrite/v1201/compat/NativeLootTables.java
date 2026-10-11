package dev.meldrite.v1201.compat;

import net.minecraft.loot.LootDataKey;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.util.Identifier;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.CraftLootTable;

public final class NativeLootTables {
    private NativeLootTables() {}
    public static CraftLootTable get(LootManager manager, Identifier id) {
        var table = manager.getElement(new LootDataKey<>(LootDataType.LOOT_TABLES, id));
        return table == null ? null : new CraftLootTable(new NamespacedKey(id.getNamespace(), id.getPath()), table);
    }
}
