package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeBlockStateCacheAccess;
import dev.meldrite.v1201.perf.PerformanceSettings;
import dev.meldrite.v1201.perf.SectionCollisionData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Paper's derived section metadata. Fabric keeps its native per-state collision traversal. */
@Mixin(ChunkSection.class)
public abstract class ChunkSectionCollisionCacheMixin {
    @Shadow @Final private PalettedContainer<BlockState> blockStateContainer;
    @Shadow private short nonEmptyBlockCount;

    protected int specialCollidingBlocks;
    private long[] knownBlockCollisionData;

    public final boolean hasSpecialCollidingBlocks() { return specialCollidingBlocks != 0; }

    @Unique private static long meldrite$extractKnownBlockInfo(int blockIndex, long value) {
        return (value >>> ((blockIndex & 31) << 1)) & 3L;
    }

    public final long getKnownBlockInfo(int blockIndex) {
        return knownBlockCollisionData == null ? 0L : meldrite$extractKnownBlockInfo(blockIndex, knownBlockCollisionData[blockIndex >>> 5]);
    }

    public final long getKnownBlockInfoHorizontalRaw(int localY, int localZ) {
        return knownBlockCollisionData == null ? 0L : knownBlockCollisionData[((localZ << 4) | (localY << 8)) >>> 5];
    }

    @Inject(method = "calculateCounts", at = @At("RETURN"))
    private void meldrite$rebuildCollisionData(CallbackInfo callback) {
        specialCollidingBlocks = 0;
        if (nonEmptyBlockCount == 0) {
            knownBlockCollisionData = null;
            return;
        }
        knownBlockCollisionData = new long[128];
        if (PerformanceSettings.COLLISION_CACHE) {
            specialCollidingBlocks = SectionCollisionData.rebuild(blockStateContainer, knownBlockCollisionData);
            return;
        }
        for (int index = 0; index < 4096; ++index) {
            BlockState state = blockStateContainer.get(index & 15, index >>> 8, (index >>> 4) & 15);
            meldrite$setKnownBlockInfo(index, state);
            if (meldrite$isSpecial(state)) ++specialCollidingBlocks;
        }
    }

    @Inject(method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", at = @At("RETURN"))
    private void meldrite$updateCollisionData(int x, int y, int z, BlockState state, boolean lock,
                                             CallbackInfoReturnable<BlockState> callback) {
        updateKnownBlockInfo(x | (z << 4) | (y << 8), callback.getReturnValue(), state);
    }

    public void updateKnownBlockInfo(int blockIndex, BlockState from, BlockState to) {
        if (meldrite$isSpecial(from)) --specialCollidingBlocks;
        if (meldrite$isSpecial(to)) ++specialCollidingBlocks;
        if (nonEmptyBlockCount == 0) {
            knownBlockCollisionData = null;
            return;
        }
        if (knownBlockCollisionData == null) knownBlockCollisionData = new long[128];
        meldrite$setKnownBlockInfo(blockIndex, to);
    }

    @Unique private void meldrite$setKnownBlockInfo(int blockIndex, BlockState state) {
        int arrayIndex = blockIndex >>> 5;
        int shift = (blockIndex & 31) << 1;
        knownBlockCollisionData[arrayIndex] = (knownBlockCollisionData[arrayIndex] & ~(3L << shift))
                | (((NativeBlockStateCacheAccess) state).getBlockCollisionBehavior() << shift);
    }

    @Unique private static boolean meldrite$isSpecial(BlockState state) {
        return state.exceedsCube() || state.isOf(Blocks.MOVING_PISTON);
    }
}
