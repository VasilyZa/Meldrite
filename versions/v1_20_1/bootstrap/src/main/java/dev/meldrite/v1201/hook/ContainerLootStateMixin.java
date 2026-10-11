package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.VehicleLootState;
import dev.meldrite.v1201.compat.VehicleLootStateAccess;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootableContainerBlockEntity.class)
public abstract class ContainerLootStateMixin implements VehicleLootStateAccess {
    @Unique private final VehicleLootState meldrite$lootState = new VehicleLootState();
    @Unique private boolean meldrite$generating;
    @Override public VehicleLootState meldrite$lootState() { return meldrite$lootState; }
    @Inject(method = "serializeLootTable", at = @At("HEAD"))
    private void meldrite$write(NbtCompound nbt, CallbackInfoReturnable<Boolean> callback) { meldrite$lootState.write(nbt); }
    @Inject(method = "deserializeLootTable", at = @At("HEAD"))
    private void meldrite$read(NbtCompound nbt, CallbackInfoReturnable<Boolean> callback) { meldrite$lootState.read(nbt); }
    @Inject(method = "checkLootInteraction", at = @At("HEAD"))
    private void meldrite$start(PlayerEntity player, CallbackInfo callback) {
        meldrite$generating = ((LootableContainerBlockEntity) (Object) this).lootTableId != null;
    }
    @Inject(method = "checkLootInteraction", at = @At("RETURN"))
    private void meldrite$filled(PlayerEntity player, CallbackInfo callback) {
        if (meldrite$generating && ((LootableContainerBlockEntity) (Object) this).lootTableId == null) {
            meldrite$lootState.markFilled();
            if (player != null) meldrite$lootState.setHasPlayerLooted(player.getUuid(), true);
        }
        meldrite$generating = false;
    }
}
