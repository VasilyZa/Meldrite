package dev.meldrite.v1201.hook;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ZombieEntity.class)
public interface ZombieEntityPaperAccessMixin {
    @Accessor("ticksUntilWaterConversion") int meldrite$getWaterConversionTicks();
    @Accessor("ticksUntilWaterConversion") void meldrite$setWaterConversionTicks(int ticks);
    @Accessor("inWaterTime") void meldrite$setInWaterTime(int ticks);
    @Accessor("CONVERTING_IN_WATER") static TrackedData<Boolean> meldrite$convertingInWater() { throw new AssertionError(); }
    @Invoker("setTicksUntilWaterConversion") void meldrite$startWaterConversion(int ticks);
    @Invoker("burnsInDaylight") boolean meldrite$burnsInDaylight();
    @Invoker("shouldBreakDoors") boolean meldrite$supportsBreakingDoors();
}
