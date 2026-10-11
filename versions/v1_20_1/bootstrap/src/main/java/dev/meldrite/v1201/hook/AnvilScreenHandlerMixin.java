package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import dev.meldrite.v1201.compat.NativeAnvilState;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin implements NativeAnvilState {
    @Shadow private int repairItemUsage;
    @Shadow private String newItemName;
    @Shadow @Final private Property levelCost;
    @Unique private int meldrite$maximumRepairCost = 40;
    @Override public String meldrite$getRenameText() { return newItemName; }
    @Override public int meldrite$getRepairCostAmount() { return repairItemUsage; }
    @Override public void meldrite$setRepairCostAmount(int amount) { repairItemUsage = amount; }
    @Override public int meldrite$getRepairCost() { return levelCost.get(); }
    @Override public void meldrite$setRepairCost(int cost) { levelCost.set(cost); }
    @Override public int meldrite$getMaximumRepairCost() { return meldrite$maximumRepairCost; }
    @Override public void meldrite$setMaximumRepairCost(int cost) { meldrite$maximumRepairCost = cost; }
    @ModifyConstant(method = "updateResult", constant = @Constant(intValue = 40))
    private int meldrite$costLimit(int vanilla) { return meldrite$maximumRepairCost; }
    @ModifyConstant(method = "updateResult", constant = @Constant(intValue = 39))
    private int meldrite$renameCostLimit(int vanilla) { return meldrite$maximumRepairCost - 1; }
    @Inject(method = "updateResult", at = @At("RETURN"))
    private void meldrite$prepare(CallbackInfo ci) {
        var handler = (AnvilScreenHandler) (Object) this;
        InventoryEventBridge.prepareResult(((ForgingScreenHandlerPaperAccessMixin) this).meldrite$getPlayer(), handler, handler.getResultSlotIndex());
    }
}
