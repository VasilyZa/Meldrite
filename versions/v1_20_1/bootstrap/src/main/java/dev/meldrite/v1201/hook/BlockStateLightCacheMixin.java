package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeBlockStateCacheAccess;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkLightProvider.class)
public abstract class BlockStateLightCacheMixin {
    @Inject(method = "isTrivialForLighting", at = @At("HEAD"), cancellable = true)
    private static void meldrite$conditionalLightShape(BlockState state, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(!((NativeBlockStateCacheAccess) state).isConditionallyFullOpaque());
    }
}
