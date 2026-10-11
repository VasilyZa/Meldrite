package dev.meldrite.v1201.hook;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BeehiveBlockEntity.class)
public interface BeehiveBlockEntityAccessMixin {
    @Accessor("flowerPos") BlockPos meldrite$flower();
    @Accessor("flowerPos") void meldrite$flower(BlockPos value);
    @Invoker("tryReleaseBee") List<Entity> meldrite$tryReleaseBee(BlockState state, BeehiveBlockEntity.BeeState releaseState);
}
