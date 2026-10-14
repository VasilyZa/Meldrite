package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.perf.ChunkHolderSnapshot;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keep fastutil's complete clone contract, replacing only its large array copies.
 * Owner-less selectors are required because Mixin MemberInfo rejects array owners.
 * In fastutil 8.5.9: Object.clone is #0; key, value, link clones are #1, #2, #3.
 */
@Mixin(value = Long2ObjectLinkedOpenHashMap.class, remap = false, priority = 900)
public abstract class ChunkHolderMapPerformanceMixin {
    @WrapOperation(method = "clone()Lit/unimi/dsi/fastutil/longs/Long2ObjectLinkedOpenHashMap;",
            at = {@At(value = "INVOKE", target = "clone()Ljava/lang/Object;", ordinal = 1, remap = false),
                    @At(value = "INVOKE", target = "clone()Ljava/lang/Object;", ordinal = 3, remap = false)},
            remap = false, require = 0)
    private Object meldrite$copyLongArray(long[] source, Operation<Object> original) {
        return ChunkHolderSnapshot.shouldCopy(source.length)
                ? ChunkHolderSnapshot.copy(source) : original.call((Object) source);
    }

    @WrapOperation(method = "clone()Lit/unimi/dsi/fastutil/longs/Long2ObjectLinkedOpenHashMap;",
            at = @At(value = "INVOKE", target = "clone()Ljava/lang/Object;", ordinal = 2, remap = false),
            remap = false, require = 0)
    private Object meldrite$copyValueArray(Object[] source, Operation<Object> original) {
        return ChunkHolderSnapshot.shouldCopy(source.length)
                ? ChunkHolderSnapshot.copy(source) : original.call((Object) source);
    }
}
