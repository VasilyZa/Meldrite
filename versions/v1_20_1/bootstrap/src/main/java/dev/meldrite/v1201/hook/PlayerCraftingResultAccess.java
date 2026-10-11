package dev.meldrite.v1201.hook;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerScreenHandler.class)
public interface PlayerCraftingResultAccess {
    @Accessor("craftingResult") CraftingResultInventory meldrite$getResult();
}
