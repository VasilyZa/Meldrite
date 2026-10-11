package dev.meldrite.v1201.hook;

import net.minecraft.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntitySnapshotMixin {
    @Inject(method = "updateListeners", at = @At("HEAD"), cancellable = true)
    private void meldrite$skipDetachedNotification(CallbackInfo callback) {
        if (((SignBlockEntity) (Object) this).getWorld() == null) callback.cancel();
    }
}
