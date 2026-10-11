package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.ExplosiveProjectileNativeAccess;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExplosiveProjectileEntity.class)
public abstract class ExplosiveProjectilePaperStateMixin implements ExplosiveProjectileNativeAccess {
    @Unique private float meldrite$yield = 1.0F;
    @Unique private boolean meldrite$incendiary = true;

    @Override public float meldrite$getYield() { return meldrite$yield; }
    @Override public void meldrite$setYield(float value) { meldrite$yield = value; }
    @Override public boolean meldrite$isIncendiary() { return meldrite$incendiary; }
    @Override public void meldrite$setIncendiary(boolean value) { meldrite$incendiary = value; }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) {
        if (meldrite$yield != 1.0F) nbt.putFloat("MeldriteYield", meldrite$yield);
        if (!meldrite$incendiary) nbt.putBoolean("MeldriteIncendiary", false);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("MeldriteYield")) meldrite$yield = nbt.getFloat("MeldriteYield");
        if (nbt.contains("MeldriteIncendiary")) meldrite$incendiary = nbt.getBoolean("MeldriteIncendiary");
    }
}
