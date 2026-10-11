package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.TridentNativeAccess;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
public abstract class TridentEntityPaperAccessMixin implements TridentNativeAccess {
    @Accessor("tridentStack")
    @Override public abstract ItemStack meldrite$getTridentStack();
    @Accessor("tridentStack")
    @Override public abstract void meldrite$setTridentStack(ItemStack stack);
    @Accessor("dealtDamage")
    @Override public abstract boolean meldrite$hasDealtDamage();
    @Accessor("dealtDamage")
    @Override public abstract void meldrite$setDealtDamage(boolean value);

    @Unique private boolean meldrite$glint;
    @Unique private int meldrite$loyalty;

    @Override public boolean meldrite$hasGlint() { return meldrite$glint; }
    @Override public void meldrite$setGlint(boolean value) { meldrite$glint = value; }
    @Override public int meldrite$getLoyalty() { return meldrite$loyalty; }
    @Override public void meldrite$setLoyalty(int value) { meldrite$loyalty = value; }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) {
        if (meldrite$glint) nbt.putBoolean("MeldriteGlint", true);
        if (meldrite$loyalty != 0) nbt.putInt("MeldriteLoyalty", meldrite$loyalty);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("MeldriteGlint")) meldrite$glint = nbt.getBoolean("MeldriteGlint");
        if (nbt.contains("MeldriteLoyalty")) meldrite$loyalty = nbt.getInt("MeldriteLoyalty");
    }
}
