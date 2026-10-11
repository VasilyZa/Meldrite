package dev.meldrite.v1201.hook;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameSoundMixin {
    @WrapWithCondition(method = "setHeldItemStack(Lnet/minecraft/item/ItemStack;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    private boolean meldrite$itemSound(ItemFrameEntity frame, net.minecraft.sound.SoundEvent sound, float volume, float pitch) { return dev.meldrite.v1201.compat.NativeItemFrameItems.playSound(); }
}
