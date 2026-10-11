package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingPrepareMixin {
    @ModifyVariable(method = "updateResult", ordinal = 0, at = @At(value = "LOAD", ordinal = 0))
    private static ItemStack meldrite$prepare(ItemStack output, ScreenHandler handler, World world,
            PlayerEntity player, RecipeInputInventory input, CraftingResultInventory result) {
        return InventoryEventBridge.prepareCraft(player, handler, input, result, output, world);
    }
}
