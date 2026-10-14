package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.meldrite.v1201.perf.TrackingBatch;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class TrackingPerformanceMixin {
    @Shadow @Final private ServerWorld world;
    @WrapMethod(method = "tickEntityMovement")
    private void meldrite$trackingBatch(Operation<Void> original) {
        TrackingBatch.run((ThreadedAnvilChunkStorage) (Object) this, world.getPlayers(), () -> original.call());
    }
}
