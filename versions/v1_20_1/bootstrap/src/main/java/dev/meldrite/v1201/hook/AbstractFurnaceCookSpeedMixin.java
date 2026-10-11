package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeFurnaceCookSpeedAccess;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.World;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceCookSpeedMixin implements NativeFurnaceCookSpeedAccess {
    @Unique private static final String MELDRITE_COOK_SPEED_KEY = "Paper.CookSpeedMultiplier";
    @Unique private double meldrite$cookSpeedMultiplier = 1.0D;

    @Override public double meldrite$getCookSpeedMultiplier() { return meldrite$cookSpeedMultiplier; }

    @Override public void meldrite$setCookSpeedMultiplier(double multiplier) {
        meldrite$cookSpeedMultiplier = multiplier;
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void meldrite$writeCookSpeed(NbtCompound nbt, CallbackInfo callback) {
        if (meldrite$cookSpeedMultiplier != 1.0D) nbt.putDouble(MELDRITE_COOK_SPEED_KEY, meldrite$cookSpeedMultiplier);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void meldrite$readCookSpeed(NbtCompound nbt, CallbackInfo callback) {
        meldrite$setCookSpeedMultiplier(nbt.contains(MELDRITE_COOK_SPEED_KEY)
                ? nbt.getDouble(MELDRITE_COOK_SPEED_KEY) : 1.0D);
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    private void meldrite$replaceRecipeCounts(NbtCompound nbt, CallbackInfo callback) {
        ((AbstractFurnaceBlockEntity) (Object) this).recipesUsed.clear();
    }

    @Inject(method = "getCookTime", at = @At("RETURN"), cancellable = true)
    private static void meldrite$scaleRecipeTime(World world, AbstractFurnaceBlockEntity furnace,
                                                CallbackInfoReturnable<Integer> callback) {
        double multiplier = ((NativeFurnaceCookSpeedAccess) furnace).meldrite$getCookSpeedMultiplier();
        if (multiplier != 1.0D) callback.setReturnValue((int) Math.ceil(callback.getReturnValue() / multiplier));
    }
}
