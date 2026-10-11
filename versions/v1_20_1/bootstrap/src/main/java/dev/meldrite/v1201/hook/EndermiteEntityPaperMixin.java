package dev.meldrite.v1201.hook;
import dev.meldrite.v1201.compat.NativeEndermiteState;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EndermiteEntity.class)
public abstract class EndermiteEntityPaperMixin implements NativeEndermiteState {
    @Unique private boolean meldrite$playerSpawned;
    @Override public boolean meldrite$isPlayerSpawned() { return meldrite$playerSpawned; }
    @Override public void meldrite$playerSpawned(boolean value) { meldrite$playerSpawned = value; }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL")) private void meldrite$write(NbtCompound nbt, CallbackInfo ci) { if (meldrite$playerSpawned) nbt.putBoolean("PlayerSpawned", true); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL")) private void meldrite$read(NbtCompound nbt, CallbackInfo ci) { meldrite$playerSpawned = nbt.getBoolean("PlayerSpawned"); }
}
