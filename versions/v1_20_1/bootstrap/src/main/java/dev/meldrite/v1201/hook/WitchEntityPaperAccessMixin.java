package dev.meldrite.v1201.hook;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.WitchEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WitchEntity.class)
public interface WitchEntityPaperAccessMixin {
    @Accessor("drinkTimeLeft") int meldrite$getDrinkTimeLeft();
    @Accessor("drinkTimeLeft") void meldrite$setDrinkTimeLeft(int ticks);
    @Accessor("DRINKING_SPEED_PENALTY_MODIFIER") static EntityAttributeModifier meldrite$drinkingSpeedPenalty() { throw new AssertionError(); }
}
