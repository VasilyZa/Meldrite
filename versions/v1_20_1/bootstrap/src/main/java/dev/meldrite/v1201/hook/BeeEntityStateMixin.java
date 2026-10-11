package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeBeeState;
import net.kyori.adventure.util.TriState;
import net.minecraft.entity.passive.BeeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BeeEntity.class)
public abstract class BeeEntityStateMixin implements NativeBeeState {
    @Unique private TriState meldrite$rollingOverride = TriState.NOT_SET;
    @Override public TriState meldrite$rollingOverride() { return meldrite$rollingOverride; }
    @Override public void meldrite$rollingOverride(TriState value) {
        meldrite$rollingOverride = java.util.Objects.requireNonNull(value, "rolling");
        var access = (BeeEntityPaperAccessMixin) this;
        access.meldrite$rolling(access.meldrite$isRolling());
    }
    @ModifyVariable(method = "setNearTarget", at = @At("HEAD"), argsOnly = true)
    private boolean meldrite$applyRollingOverride(boolean value) { return meldrite$rollingOverride.toBooleanOrElse(value); }
}
