package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpellcastingIllagerEntity.class)
public interface SpellcastingIllagerPaperAccessMixin {
    @Invoker("getSpell") SpellcastingIllagerEntity.Spell meldrite$getSpell();
}
