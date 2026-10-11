package dev.meldrite.v1201.hook;

import java.util.Set;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArrowEntity.class)
public interface ArrowEntityAccess {
    @Accessor("potion") Potion meldrite$getPotion();
    @Accessor("potion") void meldrite$setPotion(Potion value);
    @Accessor("effects") Set<StatusEffectInstance> meldrite$getEffects();
    @Invoker("initColor") void meldrite$initColor();
    @Invoker("setColor") void meldrite$setNativeColor(int color);
}
