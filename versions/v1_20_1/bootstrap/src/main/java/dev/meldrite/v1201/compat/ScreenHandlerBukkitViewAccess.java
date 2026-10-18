package dev.meldrite.v1201.compat;

import net.minecraft.screen.ScreenHandlerType;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;
import org.jetbrains.annotations.Nullable;

public interface ScreenHandlerBukkitViewAccess {
    /** Native menu types are optional, including for entity and custom menus. */
    @Nullable ScreenHandlerType<?> meldrite$getNullableType();
    CraftInventoryView meldrite$getBukkitView();
    void meldrite$setBukkitView(CraftInventoryView view);
}
