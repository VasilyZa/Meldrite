package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeBlockStateCacheAccess;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mirrors Paper's MC-Utils/Starlight state caches and applies mutations to native queries. */
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class BlockStatePaperCacheMixin implements NativeBlockStateCacheAccess {
    @Shadow protected AbstractBlock.AbstractBlockState.ShapeCache shapeCache;
    @Shadow public abstract boolean isOpaque();
    @Shadow public abstract boolean hasSidedTransparency();

    protected boolean conditionallyFullOpaque;
    protected int opacityIfCached = -1;
    protected boolean shapeExceedsCube = true;
    // Paper's two-bit classification: empty, full cube, unknown, special.
    private long blockCollisionBehavior = 3;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void meldrite$initializeLightShape(CallbackInfo callback) {
        conditionallyFullOpaque = isOpaque() && hasSidedTransparency();
    }

    @Inject(method = "initShapeCache", at = @At("RETURN"))
    private void meldrite$rebuildPaperCaches(CallbackInfo callback) {
        shapeExceedsCube = shapeCache == null || shapeCache.exceedsCube;
        opacityIfCached = shapeCache == null || conditionallyFullOpaque ? -1 : shapeCache.lightSubtracted;
        BlockState state = (BlockState) (Object) this;
        if (shapeExceedsCube || state.isOf(Blocks.MOVING_PISTON)) {
            blockCollisionBehavior = 3;
        } else {
            // Only context-independent cached states reach this branch. A modded state may
            // still reject a context-free query; unknown preserves its per-position checks.
            try {
                var shape = state.getCollisionShape(null, null, null);
                if (shape == null) blockCollisionBehavior = 2;
                else if (shape.isEmpty()) blockCollisionBehavior = 0;
                else {
                    var boxes = shape.simplify().getBoundingBoxes();
                    blockCollisionBehavior = boxes.size() == 1 && boxes.get(0).equals(new Box(0, 0, 0, 1, 1, 1)) ? 1 : 2;
                }
            } catch (Exception unsupportedContext) {
                blockCollisionBehavior = 2;
            }
        }
    }

    @Inject(method = "getOpacity", at = @At("HEAD"), cancellable = true)
    private void meldrite$cachedOpacity(BlockView world, BlockPos position, CallbackInfoReturnable<Integer> callback) {
        if (opacityIfCached >= 0) callback.setReturnValue(opacityIfCached);
    }

    @Inject(method = "exceedsCube", at = @At("HEAD"), cancellable = true)
    private void meldrite$cachedCollisionBounds(CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(shapeExceedsCube);
    }

    @Override public boolean isConditionallyFullOpaque() { return conditionallyFullOpaque; }
    @Override public int getOpacityIfCached() { return opacityIfCached; }
    @Override public boolean shapeExceedsCube() { return shapeExceedsCube; }
    @Override public final long getBlockCollisionBehavior() { return blockCollisionBehavior; }
}
