package dev.meldrite.v1201.hook;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.nbt.NbtCompound;
import org.bukkit.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityPaperStateMixin implements dev.meldrite.v1201.compat.ExperienceOrbNativeAccess {
    @Unique private java.util.UUID meldrite$triggerEntityId;
    @Unique private java.util.UUID meldrite$sourceEntityId;
    @Unique private ExperienceOrb.SpawnReason meldrite$spawnReason = ExperienceOrb.SpawnReason.UNKNOWN;
    @Accessor("orbAge") public abstract int meldrite$getOrbAge();
    @Accessor("orbAge") public abstract void meldrite$setOrbAge(int value);
    @Override @Accessor("amount") public abstract int meldrite$getAmount();
    @Override @Accessor("amount") public abstract void meldrite$setAmount(int value);
    @Override public java.util.UUID meldrite$getTriggerEntityId() { return meldrite$triggerEntityId; }
    @Override public void meldrite$setTriggerEntityId(java.util.UUID value) { meldrite$triggerEntityId = value; }
    @Override public java.util.UUID meldrite$getSourceEntityId() { return meldrite$sourceEntityId; }
    @Override public void meldrite$setSourceEntityId(java.util.UUID value) { meldrite$sourceEntityId = value; }
    @Override public ExperienceOrb.SpawnReason meldrite$getSpawnReason() { return meldrite$spawnReason; }
    @Override public void meldrite$setSpawnReason(ExperienceOrb.SpawnReason value) { meldrite$spawnReason = value == null ? ExperienceOrb.SpawnReason.UNKNOWN : value; }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) {
        if (meldrite$sourceEntityId == null && meldrite$triggerEntityId == null && meldrite$spawnReason == ExperienceOrb.SpawnReason.UNKNOWN) return;
        NbtCompound data = new NbtCompound();
        if (meldrite$sourceEntityId != null) data.putUuid("source", meldrite$sourceEntityId);
        if (meldrite$triggerEntityId != null) data.putUuid("trigger", meldrite$triggerEntityId);
        data.putString("reason", meldrite$spawnReason.name());
        nbt.put("Paper.ExpData", data);
    }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains("Paper.ExpData")) return;
        NbtCompound data = nbt.getCompound("Paper.ExpData");
        meldrite$sourceEntityId = data.containsUuid("source") ? data.getUuid("source") : null;
        meldrite$triggerEntityId = data.containsUuid("trigger") ? data.getUuid("trigger") : null;
        if (data.contains("reason")) {
            try { meldrite$spawnReason = ExperienceOrb.SpawnReason.valueOf(data.getString("reason")); }
            catch (IllegalArgumentException ignored) { meldrite$spawnReason = ExperienceOrb.SpawnReason.UNKNOWN; }
        }
    }
}
