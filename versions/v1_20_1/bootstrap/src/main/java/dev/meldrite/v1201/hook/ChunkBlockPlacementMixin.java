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

@Mixin(WorldChunk.class)
public abstract class ChunkBlockPlacementMixin {
    @WrapOperation(method = "setBlockState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;onBlockAdded(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V"))
    private void meldrite$onBlockAdded(BlockState state, World world, BlockPos position, BlockState previous, boolean moved,
            Operation<Void> original) {
        if (NativeBlockPlacement.placementEnabled(world, position, state)) original.call(state, world, position, previous, moved);
    }
}
