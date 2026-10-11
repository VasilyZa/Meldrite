package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeNearbyPlayerSource;
import dev.meldrite.v1201.compat.NativePlayerDistanceAccess;
import net.minecraft.server.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Only the loading tracker receives weights; vanilla spawning-distance tracking is untouched. */
@Mixin(targets = "net.minecraft.server.world.ChunkTicketManager$DistanceFromNearestPlayerTracker")
public abstract class WeightedPlayerDistanceSourceMixin {
    @Shadow @Final private ChunkTicketManager field_17462;
    @Inject(method = "getInitialLevel", at = @At("RETURN"), cancellable = true)
    private void meldrite$weightedSource(long chunk, CallbackInfoReturnable<Integer> ci) {
        var manager = (NativePlayerDistanceAccess) field_17462;
        if ((Object) this instanceof NativeNearbyPlayerSource && manager.meldrite$weightedViewSources()) ci.setReturnValue(manager.meldrite$viewSourceLevel(chunk));
    }
}
