package dev.meldrite.v1201.hook;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CraftingScreenHandler.class)
public interface CraftingScreenHandlerAccess {
    @Accessor("input") RecipeInputInventory meldrite$getInput();
    @Accessor("result") CraftingResultInventory meldrite$getResult();
    @Invoker("updateResult") static void meldrite$updateResult(net.minecraft.screen.ScreenHandler handler,
            net.minecraft.world.World world, net.minecraft.entity.player.PlayerEntity player,
            RecipeInputInventory input, CraftingResultInventory result) { throw new AssertionError("Mixin invoker not applied"); }
}
