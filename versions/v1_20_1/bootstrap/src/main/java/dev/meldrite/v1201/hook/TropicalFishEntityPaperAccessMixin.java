package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.TropicalFishEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TropicalFishEntity.class)
public interface TropicalFishEntityPaperAccessMixin {
    @Invoker("setTropicalFishVariant") void meldrite$setVariant(int variant);
}
