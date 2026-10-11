package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BeeEntity.class)
public interface BeeEntityPaperAccessMixin {
    @Accessor("hivePos") void meldrite$hive(BlockPos pos);
    @Accessor("cannotEnterHiveTicks") int meldrite$hiveDelay();
    @Accessor("cropsGrownSincePollination") int meldrite$crops();
    @Accessor("cropsGrownSincePollination") void meldrite$crops(int value);
    @Accessor("ticksSincePollination") int meldrite$pollinationTicks();
    @Accessor("ticksSincePollination") void meldrite$pollinationTicks(int value);
    @Invoker("setHasNectar") void meldrite$nectar(boolean value);
    @Invoker("setHasStung") void meldrite$stung(boolean value);
    @Invoker("isNearTarget") boolean meldrite$isRolling();
    @Invoker("setNearTarget") void meldrite$rolling(boolean value);
}
