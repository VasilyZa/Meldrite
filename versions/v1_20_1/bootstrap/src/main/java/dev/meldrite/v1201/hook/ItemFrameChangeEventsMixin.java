package dev.meldrite.v1201.hook;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent.ItemFrameChangeAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Cancellable;
@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameChangeEventsMixin {
    @WrapOperation(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;setHeldItemStack(Lnet/minecraft/item/ItemStack;)V"))
    private void meldrite$place(ItemFrameEntity frame, ItemStack item, Operation<Void> original, @Local(argsOnly = true) PlayerEntity player, @Cancellable CallbackInfoReturnable<ActionResult> callback) {
        var event = dev.meldrite.v1201.compat.NativeDecorationEvents.itemChange(frame, player, item, ItemFrameChangeAction.PLACE);
        if (event != null && event.isCancelled()) { callback.setReturnValue(ActionResult.FAIL); return; }
        original.call(frame, event == null ? item : org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(event.getItemStack()));
    }
    @Inject(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;getRotateItemSound()Lnet/minecraft/sound/SoundEvent;"), cancellable = true)
    private void meldrite$rotate(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> callback) {
        var frame = (ItemFrameEntity) (Object) this; var event = dev.meldrite.v1201.compat.NativeDecorationEvents.itemChange(frame, player, frame.getHeldItemStack(), ItemFrameChangeAction.ROTATE);
        if (event == null) return; if (event.isCancelled()) { callback.setReturnValue(ActionResult.FAIL); return; }
        dev.meldrite.v1201.compat.NativeItemFrameItems.set(frame, org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(event.getItemStack()), false);
    }
    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;dropHeldStack(Lnet/minecraft/entity/Entity;Z)V"), cancellable = true)
    private void meldrite$remove(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        var frame = (ItemFrameEntity) (Object) this; if (!(source.getAttacker() instanceof PlayerEntity player)) return;
        var event = dev.meldrite.v1201.compat.NativeDecorationEvents.itemChange(frame, player, frame.getHeldItemStack(), ItemFrameChangeAction.REMOVE);
        if (event == null) return; if (event.isCancelled()) { callback.setReturnValue(true); return; }
        dev.meldrite.v1201.compat.NativeItemFrameItems.set(frame, org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(event.getItemStack()), false);
    }
}
