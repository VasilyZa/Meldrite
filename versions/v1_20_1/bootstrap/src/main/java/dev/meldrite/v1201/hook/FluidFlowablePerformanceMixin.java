package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.perf.FluidPassageCache;
import dev.meldrite.v1201.perf.PerformanceSettings;
import dev.meldrite.v1201.perf.FluidStateTransitionCache;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Keeps the original shape calculation and virtual fluid/default-state calls. */
@Mixin(value = FlowableFluid.class, priority = 900)
public abstract class FluidFlowablePerformanceMixin {
    @Unique
    private static final ThreadLocal<?> meldrite$uncachedPassage = new ThreadLocal<>();
    // Separate fluid instances also keep results contributed by other mixins apart.
    @Unique
    private final ThreadLocal<FluidPassageCache<BlockState>> meldrite$passages =
            ThreadLocal.withInitial(FluidPassageCache::new);
    @Unique
    private final ThreadLocal<FluidStateTransitionCache<FluidState>> meldrite$transitions =
            ThreadLocal.withInitial(FluidStateTransitionCache::new);

    @WrapMethod(method = "receivesFlow")
    private boolean meldrite$cachedPassage(Direction face, BlockView world, BlockPos pos,
                                          BlockState state, BlockPos fromPos, BlockState fromState,
                                          Operation<Boolean> original) {
        if (!PerformanceSettings.FLUID || state.getBlock().hasDynamicBounds() || fromState.getBlock().hasDynamicBounds()) {
            return original.call(face, world, pos, state, fromPos, fromState);
        }
        FluidPassageCache<BlockState> cache = meldrite$passages.get();
        int cached = cache.get(state, fromState, face.ordinal());
        if (cached != FluidPassageCache.MISS) {
            return cached != 0;
        }
        boolean result = original.call(face, world, pos, state, fromPos, fromState);
        cache.put(state, fromState, face.ordinal(), result);
        return result;
    }

    // The original method already treats a null table as its live shape path.
    // Only this exact vanilla field is replaced: another implementation (such as
    // Lithium's) without it still runs through the wrapping method above.
    @WrapOperation(method = "receivesFlow", at = @At(value = "FIELD",
            target = "Lnet/minecraft/fluid/FlowableFluid;field_15901:Ljava/lang/ThreadLocal;"), require = 0)
    private ThreadLocal<?> meldrite$skipVanillaLru(Operation<ThreadLocal<?>> original) {
        return PerformanceSettings.FLUID ? meldrite$uncachedPassage : original.call();
    }

    @WrapOperation(method = {"getFlowing(IZ)Lnet/minecraft/fluid/FluidState;",
            "getStill(Z)Lnet/minecraft/fluid/FluidState;"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/fluid/FluidState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"))
    private Object meldrite$cachedTransition(FluidState state, Property<?> property,
                                            Comparable<?> value, Operation<Object> original) {
        if (!PerformanceSettings.FLUID) return original.call(state, property, value);
        int transition = meldrite$transition(property, value);
        if (transition < 0) {
            return original.call(state, property, value);
        }
        FluidStateTransitionCache<FluidState> cache = meldrite$transitions.get();
        FluidState cached = cache.get(state, transition);
        if (cached != null) {
            return cached;
        }
        Object result = original.call(state, property, value);
        if (result instanceof FluidState fluidState) {
            cache.put(state, transition, fluidState);
        }
        return result;
    }

    @Unique
    private static int meldrite$transition(Property<?> property, Comparable<?> value) {
        if (property == FlowableFluid.FALLING && value instanceof Boolean falling) {
            return falling ? 1 : 0;
        }
        if (property == FlowableFluid.LEVEL && value instanceof Integer level && level >= 1 && level <= 8) {
            return level + 1;
        }
        // Invalid values and any properties introduced by another mixin retain
        // their original transition and exception behavior, including repeats.
        return -1;
    }
}
