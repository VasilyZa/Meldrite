package dev.meldrite.v1201.hook;

import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DoubleInventory.class)
public interface DoubleInventoryAccessMixin {
    @Accessor("first") Inventory meldrite$first();
    @Accessor("second") Inventory meldrite$second();
}
