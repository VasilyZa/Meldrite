package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeBlockEntityPersistentDataAccess;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataTypeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityPersistentDataMixin implements NativeBlockEntityPersistentDataAccess {
    @Unique private static final CraftPersistentDataTypeRegistry MELDRITE_TYPES = new CraftPersistentDataTypeRegistry();
    @Unique private CraftPersistentDataContainer meldrite$persistentData;

    @Override public CraftPersistentDataContainer meldrite$getPersistentDataContainer() {
        if (meldrite$persistentData == null) meldrite$persistentData = new CraftPersistentDataContainer(MELDRITE_TYPES);
        return meldrite$persistentData;
    }

    @Override public void meldrite$loadPersistentData(NbtCompound values) {
        CraftPersistentDataContainer container = meldrite$getPersistentDataContainer();
        container.clear();
        container.putAll(values);
    }

    @Inject(method = "createNbt", at = @At("RETURN"))
    private void meldrite$writePersistentData(CallbackInfoReturnable<NbtCompound> callback) {
        CraftPersistentDataContainer container = meldrite$getPersistentDataContainer();
        if (!container.isEmpty()) callback.getReturnValue().put("BukkitValues", container.toTagCompound());
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void meldrite$readPersistentData(NbtCompound nbt, CallbackInfo callback) {
        if (nbt.contains("BukkitValues", NbtElement.COMPOUND_TYPE)) {
            meldrite$loadPersistentData(nbt.getCompound("BukkitValues"));
        } else if (meldrite$persistentData != null) {
            meldrite$persistentData.clear();
        }
    }

    // Several vanilla tiles do not call the base readNbt method. This factory
    // is also the native chunk-load path, so restore PDC after virtual loading.
    @Inject(method = "createFromNbt", at = @At("RETURN"))
    private static void meldrite$restorePersistentData(BlockPos pos, BlockState state, NbtCompound nbt,
            CallbackInfoReturnable<BlockEntity> callback) {
        if (callback.getReturnValue() instanceof NativeBlockEntityPersistentDataAccess tile) {
            tile.meldrite$loadPersistentData(nbt.getCompound("BukkitValues"));
        }
    }
}
