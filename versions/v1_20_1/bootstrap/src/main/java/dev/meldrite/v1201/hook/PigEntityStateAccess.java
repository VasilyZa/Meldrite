package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.PigEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PigEntity.class)
public interface PigEntityStateAccess {
    @Accessor("saddledComponent") net.minecraft.entity.SaddledComponent meldrite$steering();
}
