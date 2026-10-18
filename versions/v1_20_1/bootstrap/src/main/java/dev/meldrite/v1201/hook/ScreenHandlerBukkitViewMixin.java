package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import dev.meldrite.v1201.compat.ScreenHandlerBukkitViewAccess;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerBukkitViewMixin implements ScreenHandlerBukkitViewAccess {
    @Unique private CraftInventoryView meldrite$bukkitView;

    @Override @Accessor("type") public abstract ScreenHandlerType<?> meldrite$getNullableType();
    @Override public CraftInventoryView meldrite$getBukkitView() { return meldrite$bukkitView; }
    @Override public void meldrite$setBukkitView(CraftInventoryView view) { meldrite$bukkitView = view; }

    public CraftInventoryView getBukkitView() {
        return (CraftInventoryView) InventoryEventBridge.bukkitView((ScreenHandler) (Object) this);
    }
}
