package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.perf.ChunkCompletionBudget;
import dev.meldrite.v1201.perf.PerformanceSettings;
import dev.meldrite.v1201.perf.ChunkHolderSnapshot;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ThreadedAnvilChunkStorage.class, priority = 900)
public abstract class ThreadedChunkPerformanceMixin {
    @Shadow @Final private ServerWorld world;
    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;
    @Unique private ChunkCompletionBudget meldrite$completionBudget;

    @Unique
    private ChunkCompletionBudget meldrite$budget() {
        if (meldrite$completionBudget == null) {
            meldrite$completionBudget = new ChunkCompletionBudget(world, mainThreadExecutor);
        }
        return meldrite$completionBudget;
    }

    @WrapOperation(method = "updateHolderMap", at = @At(value = "INVOKE",
            target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectLinkedOpenHashMap;clone()Lit/unimi/dsi/fastutil/longs/Long2ObjectLinkedOpenHashMap;",
            remap = false), require = 0)
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> meldrite$snapshot(
            Long2ObjectLinkedOpenHashMap<ChunkHolder> source,
            Operation<Long2ObjectLinkedOpenHashMap<ChunkHolder>> original) {
        ChunkHolderSnapshot.enter();
        try { return original.call(source); }
        finally { ChunkHolderSnapshot.exit(); }
    }

    @WrapOperation(method = "makeChunkTickable", at = @At(value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"),
            require = 0)
    private CompletableFuture<?> meldrite$budgetCompletion(CompletableFuture<?> future,
            Function<?, ?> function, Executor executor, Operation<CompletableFuture<?>> original,
            ChunkHolder holder) {
        Executor selected = PerformanceSettings.CHUNK_COMPLETION && executor == mainThreadExecutor
                ? meldrite$budget().executor(holder.getPos()) : executor;
        return original.call(future, function, selected);
    }

    @WrapMethod(method = "tick")
    private void meldrite$tickCompletions(BooleanSupplier shouldKeepTicking, Operation<Void> original) {
        if (PerformanceSettings.CHUNK_COMPLETION) meldrite$budget().tick();
        original.call(shouldKeepTicking);
    }

    @WrapMethod(method = "close")
    private void meldrite$closeCompletions(Operation<Void> original) throws IOException {
        if (meldrite$completionBudget != null) meldrite$completionBudget.close();
        original.call();
    }
}
