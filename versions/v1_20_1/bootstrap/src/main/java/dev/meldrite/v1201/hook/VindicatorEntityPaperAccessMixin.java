package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.VindicatorEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VindicatorEntity.class)
public interface VindicatorEntityPaperAccessMixin {
    @Accessor("johnny") boolean meldrite$isJohnny();
    @Accessor("johnny") void meldrite$setJohnny(boolean value);
}
