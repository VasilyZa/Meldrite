package dev.meldrite.v1201.hook;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ForgingScreenHandler.class)
public interface ForgingScreenHandlerPaperAccessMixin {
    @Accessor("context") ScreenHandlerContext meldrite$getContext();
    @Accessor("player") PlayerEntity meldrite$getPlayer();
    @Mutable @Accessor("input") void meldrite$setInput(Inventory inventory);
    @Mutable @Accessor("output") void meldrite$setOutput(CraftingResultInventory inventory);
}
