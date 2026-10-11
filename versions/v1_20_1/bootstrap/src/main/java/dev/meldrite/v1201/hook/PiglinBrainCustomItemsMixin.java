package dev.meldrite.v1201.hook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativePiglinItems;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(PiglinBrain.class)
public abstract class PiglinBrainCustomItemsMixin {
    @WrapOperation(method = {"consumeOffHandItem", "canGather", "isWillingToTrade"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PiglinBrain;acceptsForBarter(Lnet/minecraft/item/ItemStack;)Z"))
    private static boolean meldrite$customPayment(ItemStack stack, Operation<Boolean> original, PiglinEntity piglin) { return original.call(stack) || ((NativePiglinItems) piglin).meldrite$barterItems().contains(stack.getItem()); }
    @WrapOperation(method = {"loot", "consumeOffHandItem", "canGather", "doesNotHaveGoldInOffHand"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PiglinBrain;isGoldenItem(Lnet/minecraft/item/ItemStack;)Z"))
    private static boolean meldrite$customInterest(ItemStack stack, Operation<Boolean> original, PiglinEntity piglin) { var state = (NativePiglinItems) piglin; return original.call(stack) || state.meldrite$interestItems().contains(stack.getItem()) || state.meldrite$barterItems().contains(stack.getItem()); }
}
