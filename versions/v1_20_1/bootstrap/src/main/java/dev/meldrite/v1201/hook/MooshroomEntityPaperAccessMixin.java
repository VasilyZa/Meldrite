package dev.meldrite.v1201.hook;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.passive.MooshroomEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(MooshroomEntity.class)
public interface MooshroomEntityPaperAccessMixin {
    @Accessor("stewEffect") StatusEffect meldrite$effect();
    @Accessor("stewEffect") void meldrite$effect(StatusEffect effect);
    @Accessor("stewEffectDuration") int meldrite$duration();
    @Accessor("stewEffectDuration") void meldrite$duration(int ticks);
}
