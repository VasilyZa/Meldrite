package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeBeaconRange;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds Paper's persisted custom radius to the 1.20.1 beacon implementation. */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityRangeMixin implements NativeBeaconRange {
    @Unique private static final String MELDRITE_RANGE_TAG = "Paper.Range";
    @Unique private static final ThreadLocal<Double> MELDRITE_EFFECT_RANGE = new ThreadLocal<>();
    @Unique private double meldrite$effectRange = -1.0D;
    @Override public boolean meldrite$hasEffectRangeOverride() { return meldrite$effectRange >= 0.0D; }

    @Override public double meldrite$getEffectRange() {
        double value = meldrite$effectRange;
        return value >= 0.0D ? value : ((BeaconBlockEntityAccessMixin) (Object) this).meldrite$level() * 10.0D + 10.0D;
    }

    @Override public void meldrite$setEffectRange(double range) {
        if (!Double.isFinite(range) || range < 0.0D) throw new IllegalArgumentException("Beacon range must be finite and non-negative");
        meldrite$effectRange = range;
        ((BeaconBlockEntity) (Object) this).markDirty();
    }

    @Override public void meldrite$resetEffectRange() {
        meldrite$effectRange = -1.0D;
        ((BeaconBlockEntity) (Object) this).markDirty();
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo callback) {
        var access = (BeaconBlockEntityAccessMixin) (Object) this;
        access.meldrite$level(nbt.getInt("Levels"));
        access.meldrite$primary(net.minecraft.entity.effect.StatusEffect.byRawId(nbt.getInt("Primary")));
        access.meldrite$secondary(net.minecraft.entity.effect.StatusEffect.byRawId(nbt.getInt("Secondary")));
        meldrite$effectRange = nbt.contains(MELDRITE_RANGE_TAG, 6) ? nbt.getDouble(MELDRITE_RANGE_TAG) : -1.0D;
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo callback) {
        if (meldrite$effectRange >= 0.0D) nbt.putDouble(MELDRITE_RANGE_TAG, meldrite$effectRange);
    }

    @Inject(method = "applyPlayerEffects", at = @At("HEAD"))
    private static void meldrite$begin(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, int level,
                                       net.minecraft.entity.effect.StatusEffect primary, net.minecraft.entity.effect.StatusEffect secondary, CallbackInfo callback) {
        if (world.getBlockEntity(pos) instanceof NativeBeaconRange beacon && beacon.meldrite$hasEffectRangeOverride()) {
            MELDRITE_EFFECT_RANGE.set(beacon.meldrite$getEffectRange());
        }
    }

    @Inject(method = "applyPlayerEffects", at = @At("RETURN"))
    private static void meldrite$end(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, int level,
                                     net.minecraft.entity.effect.StatusEffect primary, net.minecraft.entity.effect.StatusEffect secondary, CallbackInfo callback) {
        MELDRITE_EFFECT_RANGE.remove();
    }

    @ModifyArg(method = "applyPlayerEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(D)Lnet/minecraft/util/math/Box;"), index = 0)
    private static double meldrite$range(double vanillaRange) {
        Double range = MELDRITE_EFFECT_RANGE.get();
        return range == null ? vanillaRange : range;
    }
}
