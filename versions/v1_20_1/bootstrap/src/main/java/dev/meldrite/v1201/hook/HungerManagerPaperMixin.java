package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.bridge.HungerManagerPaperAccess;
import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Keeps the Paper hunger timing knobs on the real Fabric hunger manager. */
@Mixin(HungerManager.class)
public abstract class HungerManagerPaperMixin implements HungerManagerPaperAccess {
    private int meldrite$saturatedRegenRate = 10;
    private int meldrite$unsaturatedRegenRate = 80;
    private int meldrite$starvationRate = 80;

    @Override public int meldrite$getSaturatedRegenRate() { return meldrite$saturatedRegenRate; }
    @Override public void meldrite$setSaturatedRegenRate(int ticks) { meldrite$saturatedRegenRate = requirePositive(ticks); }
    @Override public int meldrite$getUnsaturatedRegenRate() { return meldrite$unsaturatedRegenRate; }
    @Override public void meldrite$setUnsaturatedRegenRate(int ticks) { meldrite$unsaturatedRegenRate = requirePositive(ticks); }
    @Override public int meldrite$getStarvationRate() { return meldrite$starvationRate; }
    @Override public void meldrite$setStarvationRate(int ticks) { meldrite$starvationRate = requirePositive(ticks); }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 10))
    private int meldrite$saturatedRate(int ignored) { return meldrite$saturatedRegenRate; }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 80, ordinal = 0))
    private int meldrite$unsaturatedRate(int ignored) { return meldrite$unsaturatedRegenRate; }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 80, ordinal = 1))
    private int meldrite$starvationRate(int ignored) { return meldrite$starvationRate; }

    private static int requirePositive(int ticks) {
        if (ticks < 1) throw new IllegalArgumentException("Regen rate must be positive");
        return ticks;
    }
}
