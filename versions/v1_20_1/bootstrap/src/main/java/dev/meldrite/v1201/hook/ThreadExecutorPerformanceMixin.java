package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.meldrite.v1201.perf.ChunkCompletionBudget;
import dev.meldrite.v1201.perf.PerformanceSettings;
import java.util.function.BooleanSupplier;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ThreadExecutor.class, priority = 900)
public abstract class ThreadExecutorPerformanceMixin {
    @WrapMethod(method = "runTasks(Ljava/util/function/BooleanSupplier;)V")
    private void meldrite$flushBeforeWaiting(BooleanSupplier stop, Operation<Void> original) {
        if (!PerformanceSettings.CHUNK_COMPLETION || ChunkCompletionBudget.consumeTickIdleWait()) {
            original.call(stop);
            return;
        }
        ChunkCompletionBudget.enterSynchronousWait();
        try { original.call(stop); }
        finally { ChunkCompletionBudget.exitSynchronousWait(); }
    }
}
