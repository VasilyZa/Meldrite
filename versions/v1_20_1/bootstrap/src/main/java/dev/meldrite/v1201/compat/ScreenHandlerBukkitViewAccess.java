package dev.meldrite.v1201.compat;

import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;

public interface ScreenHandlerBukkitViewAccess {
    CraftInventoryView meldrite$getBukkitView();
    void meldrite$setBukkitView(CraftInventoryView view);
}
