package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeChunkPersistentDataAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.chunk.Chunk;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.v1_20_R1.persistence.DirtyCraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chunk.class)
public abstract class ChunkPersistentDataMixin implements NativeChunkPersistentDataAccess {
    @Unique private static final CraftPersistentDataTypeRegistry MELDRITE_TYPES = new CraftPersistentDataTypeRegistry();
    public DirtyCraftPersistentDataContainer persistentDataContainer = new DirtyCraftPersistentDataContainer(MELDRITE_TYPES);

    @Override public CraftPersistentDataContainer meldrite$getPersistentDataContainer() {
        if (persistentDataContainer == null) throw new IllegalStateException("CHUNK_PERSISTENT_DATA_CONTAINER_MISSING");
        return persistentDataContainer;
    }

    @Override public void meldrite$loadPersistentData(NbtCompound values) {
        meldrite$getPersistentDataContainer().putAll(values);
    }

    @Inject(method = "needsSaving", at = @At("RETURN"), cancellable = true)
    private void meldrite$persistentDataNeedsSaving(CallbackInfoReturnable<Boolean> callback) {
        if (persistentDataContainer.dirty()) callback.setReturnValue(true);
    }

    @Inject(method = "setNeedsSaving", at = @At("RETURN"))
    private void meldrite$persistentDataSaved(boolean needsSaving, CallbackInfo callback) {
        if (!needsSaving) persistentDataContainer.dirty(false);
    }
}
