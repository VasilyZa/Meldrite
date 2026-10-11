package org.bukkit.craftbukkit.v1_20_R1.boss;

import net.minecraft.entity.boss.CommandBossBar;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.KeyedBossBar;

public class CraftKeyedBossbar extends CraftBossBar implements KeyedBossBar {
    public CraftKeyedBossbar(CommandBossBar handle) { super(handle); }
    @Override public CommandBossBar getHandle() { return (CommandBossBar) super.getHandle(); }
    @Override public NamespacedKey getKey() {
        var id = getHandle().getId();
        return new NamespacedKey(id.getNamespace(), id.getPath());
    }
}
