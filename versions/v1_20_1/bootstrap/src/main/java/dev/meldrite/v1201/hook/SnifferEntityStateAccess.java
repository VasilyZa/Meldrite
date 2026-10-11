package dev.meldrite.v1201.hook;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SnifferEntity.class)
public interface SnifferEntityStateAccess {
    @Invoker("getState") SnifferEntity.State meldrite$state();
    @Invoker("getExploredPositions") Stream<GlobalPos> meldrite$explored();
    @Invoker("addExploredPosition") SnifferEntity meldrite$explore(BlockPos pos);
    @Invoker("findSniffingTargetPos") Optional<BlockPos> meldrite$digLocation();
    @Invoker("canDig") boolean meldrite$canDig();
}
