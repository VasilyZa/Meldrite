package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.ArmorStandNativeAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandExtensionsMixin extends LivingEntity implements ArmorStandNativeAccess {
    @Unique private boolean meldrite$canMove = true;
    @Unique private boolean meldrite$canTick = true;
    @Unique private boolean meldrite$canTickSetByApi;
    protected ArmorStandExtensionsMixin(EntityType<? extends LivingEntity> type, World world) { super(type, world); }
    @Invoker("setSmall") public abstract void meldrite$setSmall(boolean small);
    @Invoker("setMarker") public abstract void meldrite$setMarker(boolean marker);
    @Accessor("disabledSlots") public abstract int meldrite$getDisabledSlots();
    @Accessor("disabledSlots") public abstract void meldrite$setDisabledSlots(int slots);
    @Invoker("isSlotDisabled") public abstract boolean meldrite$isSlotDisabled(net.minecraft.entity.EquipmentSlot slot);
    @Override public boolean meldrite$canMove() { return meldrite$canMove; }
    @Override public void meldrite$setCanMove(boolean move) { meldrite$canMove = move; }
    @Override public boolean meldrite$canTick() { return meldrite$canTick; }
    @Override public void meldrite$setCanTick(boolean tick) { meldrite$canTick = tick; meldrite$canTickSetByApi = true; }
    @Override public void move(MovementType type, Vec3d movement) { if (meldrite$canMove) super.move(type, movement); }
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void meldrite$skipDisabledTick(CallbackInfo callback) {
        if (!meldrite$canTick) {
            // Keep tracked poses and equipment coherent without advancing simulation.
            var stand = (ArmorStandEntity) (Object) this;
            var tracker = stand.getDataTracker();
            stand.setHeadRotation(tracker.get(ArmorStandEntity.TRACKER_HEAD_ROTATION));
            stand.setBodyRotation(tracker.get(ArmorStandEntity.TRACKER_BODY_ROTATION));
            stand.setLeftArmRotation(tracker.get(ArmorStandEntity.TRACKER_LEFT_ARM_ROTATION));
            stand.setRightArmRotation(tracker.get(ArmorStandEntity.TRACKER_RIGHT_ARM_ROTATION));
            stand.setLeftLegRotation(tracker.get(ArmorStandEntity.TRACKER_LEFT_LEG_ROTATION));
            stand.setRightLegRotation(tracker.get(ArmorStandEntity.TRACKER_RIGHT_LEG_ROTATION));
            ((LivingEntityPaperAccessMixin) this).meldrite$sendEquipmentChanges();
            callback.cancel();
        }
    }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$writeTickOverride(NbtCompound nbt, CallbackInfo callback) {
        if (meldrite$canTickSetByApi) nbt.putBoolean("Paper.CanTickOverride", meldrite$canTick);
    }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$readTickOverride(NbtCompound nbt, CallbackInfo callback) {
        if (nbt.contains("Paper.CanTickOverride")) {
            meldrite$canTick = nbt.getBoolean("Paper.CanTickOverride");
            meldrite$canTickSetByApi = true;
        }
    }
}
