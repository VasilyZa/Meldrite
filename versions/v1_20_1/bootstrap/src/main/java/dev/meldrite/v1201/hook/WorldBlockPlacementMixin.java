package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeBlockPlacement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public abstract class WorldBlockPlacementMixin {
    @WrapOperation(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;"))
    private BlockState meldrite$placementFlags(WorldChunk chunk, BlockPos position, BlockState state, boolean moved,
            Operation<BlockState> original,
            BlockPos originalPosition, BlockState originalState, int flags, int depth) {
        return NativeBlockPlacement.setBlockState(chunk, position, state, flags, () -> original.call(chunk, position, state, moved));
    }
}
