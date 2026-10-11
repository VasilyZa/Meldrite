package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.VehicleLootState;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.screen.ScreenHandler;

@Mixin(StorageMinecartEntity.class)
public abstract class StorageMinecartLootStateMixin implements dev.meldrite.v1201.compat.VehicleLootStateAccess {
    @Unique private final VehicleLootState meldrite$lootState = new VehicleLootState();
    @Override public VehicleLootState meldrite$lootState() { return meldrite$lootState; }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) { meldrite$lootState.write(nbt); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) { meldrite$lootState.read(nbt); }
    @Inject(method = "createMenu", at = @At("RETURN"))
    private void meldrite$markFilled(int syncId, net.minecraft.entity.player.PlayerInventory inventory,
                                     net.minecraft.entity.player.PlayerEntity player, CallbackInfoReturnable<ScreenHandler> cir) {
        if (cir.getReturnValue() != null && ((StorageMinecartEntity) (Object) this).getLootTableId() == null) meldrite$lootState.markFilled();
    }
}
