package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeShulkerLid;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerLidMixin implements NativeShulkerLid {
    @Shadow private int viewerCount;
    @Unique private boolean meldrite$opened;
    @Override public boolean meldrite$isOpen() { return meldrite$opened; }
    @Override public void meldrite$setOpen(boolean value) { meldrite$opened = value; }
    @ModifyArg(method = {"onOpen", "onClose"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addSyncedBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"), index = 3)
    private int meldrite$includeApiViewer(int count) { return count + (meldrite$opened ? 1 : 0); }
    @Inject(method = "onSyncedBlockEvent", at = @At("RETURN"))
    private void meldrite$restorePlayerCount(int type, int count, CallbackInfoReturnable<Boolean> callback) {
        var world = ((ShulkerBoxBlockEntity) (Object) this).getWorld();
        if (type == 1 && meldrite$opened && world != null && !world.isClient()) viewerCount = Math.max(0, count - 1);
    }
}
