package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.perf.ChunkHolderSnapshot;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Snapshot copying only; chunk readiness retains the native executor and wait lifecycle. */
@Mixin(value = ThreadedAnvilChunkStorage.class, priority = 900)
public abstract class ThreadedChunkPerformanceMixin {
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
}
