package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeHorseState;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityPaperStateMixin implements NativeHorseState {
    @Unique private int meldrite$maxDomestication = -1;
    @Unique private boolean meldrite$forceRearing;
    @Override public int meldrite$maxDomestication() { return meldrite$maxDomestication; }
    @Override public void meldrite$maxDomestication(int value) { meldrite$maxDomestication = value; }
    @Override public void meldrite$forceRearing(boolean value) { meldrite$forceRearing = value; ((AbstractHorseEntity) (Object) this).setAngry(value); }
    @Inject(method = "getMaxTemper", at = @At("HEAD"), cancellable = true)
    private void meldrite$temperLimit(CallbackInfoReturnable<Integer> callback) { if (meldrite$maxDomestication > 0) callback.setReturnValue(meldrite$maxDomestication); }
    @ModifyVariable(method = "setAngry", at = @At("HEAD"), argsOnly = true)
    private boolean meldrite$keepRearing(boolean angry) { return angry || meldrite$forceRearing; }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$writeTemper(NbtCompound nbt, CallbackInfo callback) { if (meldrite$maxDomestication > 0) nbt.putInt("Bukkit.MaxDomestication", meldrite$maxDomestication); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$readTemper(NbtCompound nbt, CallbackInfo callback) { meldrite$maxDomestication = nbt.contains("Bukkit.MaxDomestication", 3) && nbt.getInt("Bukkit.MaxDomestication") > 0 ? nbt.getInt("Bukkit.MaxDomestication") : -1; }
}
