package dev.meldrite.v1201.hook;

import net.minecraft.entity.TntEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TntEntity.class)
public abstract class TntEntityPaperStateMixin implements dev.meldrite.v1201.compat.TntNativeAccess {
    @Unique private float meldrite$yield = 4.0F;
    @Unique private boolean meldrite$incendiary;
    @Override @Accessor("causingEntity") public abstract net.minecraft.entity.LivingEntity meldrite$getSource();
    @Override @Accessor("causingEntity") public abstract void meldrite$setSource(net.minecraft.entity.LivingEntity value);
    @Override public float meldrite$getYield() { return meldrite$yield; }
    @Override public void meldrite$setYield(float value) { meldrite$yield = value; }
    @Override public boolean meldrite$isIncendiary() { return meldrite$incendiary; }
    @Override public void meldrite$setIncendiary(boolean value) { meldrite$incendiary = value; }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) { if (meldrite$yield != 4.0F) nbt.putFloat("MeldriteYield", meldrite$yield); if (meldrite$incendiary) nbt.putBoolean("MeldriteIncendiary", true); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) { if (nbt.contains("MeldriteYield")) meldrite$yield = nbt.getFloat("MeldriteYield"); meldrite$incendiary = nbt.getBoolean("MeldriteIncendiary"); }
}
