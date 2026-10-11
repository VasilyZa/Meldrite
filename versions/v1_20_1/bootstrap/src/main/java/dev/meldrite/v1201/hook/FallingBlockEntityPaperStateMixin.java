package dev.meldrite.v1201.hook;

import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityPaperStateMixin implements dev.meldrite.v1201.compat.FallingBlockNativeAccess {
    @Unique private boolean meldrite$autoExpire = true;
    @Override public boolean meldrite$doesAutoExpire() { return meldrite$autoExpire; }
    @Override public void meldrite$setAutoExpire(boolean value) { meldrite$autoExpire = value; }
    @Accessor("destroyedOnLanding") public abstract boolean meldrite$getCancelDrop();
    @Accessor("destroyedOnLanding") public abstract void meldrite$setCancelDrop(boolean value);
    @Override @Accessor("hurtEntities") public abstract boolean meldrite$getHurtEntities();
    @Override @Accessor("hurtEntities") public abstract void meldrite$setHurtEntities(boolean value);
    @Override @Accessor("fallHurtMax") public abstract int meldrite$getMaxDamage();
    @Override @Accessor("fallHurtMax") public abstract void meldrite$setMaxDamage(int value);
    @Override @Accessor("fallHurtAmount") public abstract float meldrite$getDamagePerBlock();
    @Override @Accessor("fallHurtAmount") public abstract void meldrite$setDamagePerBlock(float value);
    @Override @Accessor("block") public abstract net.minecraft.block.BlockState meldrite$getBlock();
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) { if (!meldrite$autoExpire) nbt.putBoolean("Paper.AutoExpire", false); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) { if (nbt.contains("Paper.AutoExpire")) meldrite$autoExpire = nbt.getBoolean("Paper.AutoExpire"); }
}
