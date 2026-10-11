package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.ZoglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ZoglinEntity.class)
public interface ZoglinEntityPaperAccessMixin {
    @Accessor("BABY") static net.minecraft.entity.data.TrackedData<Boolean> meldrite$baby() { throw new AssertionError(); }
}
