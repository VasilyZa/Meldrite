package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.perf.PathSearchBatch;
import dev.meldrite.v1201.perf.PerformanceSettings;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PathNodeNavigator.class, priority = 900)
public abstract class PathNodeNavigatorPerformanceMixin {
    @Shadow @Final private PathNodeMaker pathNodeMaker;
    @WrapMethod(method = "findPathToAny(Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/entity/ai/pathing/PathNode;Ljava/util/Map;FIF)Lnet/minecraft/entity/ai/pathing/Path;")
    private Path meldrite$indexTargets(Profiler profiler, PathNode start, Map<TargetPathNode, BlockPos> targets,
                                      float followRange, int distance, float rangeMultiplier, Operation<Path> original) {
        Class<?> evaluator = pathNodeMaker.getClass();
        boolean vanilla = evaluator == net.minecraft.entity.ai.pathing.LandPathNodeMaker.class
                || evaluator == net.minecraft.entity.ai.pathing.BirdPathNodeMaker.class
                || evaluator == net.minecraft.entity.ai.pathing.WaterPathNodeMaker.class
                || evaluator == net.minecraft.entity.ai.pathing.AmphibiousPathNodeMaker.class;
        return PathSearchBatch.run(this, targets, vanilla,
                () -> original.call(profiler, start, targets, followRange, distance, rangeMultiplier));
    }
    @WrapMethod(method = "calculateDistances")
    private float meldrite$nearestTarget(PathNode node, Set<TargetPathNode> targets, Operation<Float> original) {
        Float result = PathSearchBatch.calculate(this, node);
        return result == null ? original.call(node, targets) : result;
    }
    @Inject(method = "findPathToAny(Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/entity/ai/pathing/PathNode;Ljava/util/Map;FIF)Lnet/minecraft/entity/ai/pathing/Path;",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;stream()Ljava/util/stream/Stream;"))
    private void meldrite$finishTargetDistances(Profiler profiler, PathNode start, Map<TargetPathNode, BlockPos> targets,
                                               float followRange, int distance, float rangeMultiplier,
                                               CallbackInfoReturnable<Path> cir) {
        PathSearchBatch.finish(this);
    }
    @WrapOperation(method = "createPath", at = @At(value = "INVOKE",
            target = "Ljava/util/List;add(ILjava/lang/Object;)V"), require = 0)
    private void meldrite$appendPathNode(List<PathNode> nodes, int index, Object node, Operation<Void> original) {
        original.call(nodes, PerformanceSettings.PATH_INDEX ? nodes.size() : index, node);
    }
    @WrapOperation(method = "createPath", at = @At(value = "NEW", target = "net/minecraft/entity/ai/pathing/Path"), require = 0)
    private Path meldrite$reversePath(List<PathNode> nodes, BlockPos target, boolean reached, Operation<Path> original) {
        if (PerformanceSettings.PATH_INDEX) Collections.reverse(nodes);
        return original.call(nodes, target, reached);
    }
}
