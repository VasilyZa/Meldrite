package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.perf.ChunkCompletionBudget;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MinecraftServer.class, priority = 900)
public abstract class ThreadedServerPerformanceMixin {
    @WrapOperation(method = "runTasksTillTickEnd", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;runTasks(Ljava/util/function/BooleanSupplier;)V"),
            require = 0)
    private void meldrite$ordinaryTickWait(MinecraftServer server, BooleanSupplier stop,
                                          Operation<Void> original) {
        ChunkCompletionBudget.runTickIdleWait(() -> original.call(server, stop));
    }
}
