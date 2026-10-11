package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeViewerState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ViewerCountManager.class)
public abstract class ViewerCountPaperMixin implements NativeViewerState {
    @Unique private boolean meldrite$opened;
    @Override public boolean meldrite$isOpen() { return meldrite$opened; }
    @Override public void meldrite$setOpen(World world, BlockPos position, BlockState state, boolean value) {
        if (meldrite$opened == value) return;
        meldrite$opened = value;
        ((ViewerCountManager) (Object) this).updateViewerCount(world, position, state);
    }
    @Inject(method = "getInRangeViewerCount", at = @At("RETURN"), cancellable = true)
    private void meldrite$includeApiViewer(World world, BlockPos position, CallbackInfoReturnable<Integer> callback) {
        if (meldrite$opened) callback.setReturnValue(callback.getReturnValue() + 1);
    }
}
