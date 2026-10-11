package dev.meldrite.v1201.hook;

import java.util.UUID;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ZombieVillagerEntity.class)
public interface ZombieVillagerEntityPaperAccessMixin {
    @Accessor("conversionTimer") int meldrite$getConversionTimer();
    @Accessor("conversionTimer") void meldrite$setConversionTimer(int ticks);
    @Accessor("converter") UUID meldrite$getConverter();
    @Accessor("converter") void meldrite$setConverter(UUID uuid);
    @Accessor("CONVERTING") static TrackedData<Boolean> meldrite$converting() { throw new AssertionError(); }
    @Invoker("setConverting") void meldrite$startConversion(UUID uuid, int ticks);
}
