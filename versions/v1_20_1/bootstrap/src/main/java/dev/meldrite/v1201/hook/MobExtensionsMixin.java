package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.MobNativeAccess;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.NativeMobExtensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftMob;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobExtensionsMixin implements MobNativeAccess {
    @Shadow @Final protected GoalSelector goalSelector;
    public boolean aware = true;
    @Unique private int meldrite$targetDispatch;
    @Override public boolean meldrite$isAware() { return aware; }
    @Override public void meldrite$setAware(boolean value) { aware = value; }
    public CraftMob getBukkitMob() { return (CraftMob) NativeEntityExtensions.bukkit((MobEntity) (Object) this); }
    public net.minecraft.sound.SoundEvent getAmbientSound0() { return ((MobEntityPaperAccessMixin) this).meldrite$ambientSound(); }
    public boolean setTarget(LivingEntity target, EntityTargetEvent.TargetReason reason, boolean fireEvent) { return meldrite$setTarget(target, reason, fireEvent); }
    @Override public boolean meldrite$setTarget(LivingEntity target, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        MobEntity mob = (MobEntity) (Object) this;
        NativeEntityExtensions.requireThread(mob);
        if (mob.getTarget() == target) return false;
        if (target != null && target.getWorld() != mob.getWorld()) throw new IllegalArgumentException("TARGET_IN_DIFFERENT_WORLD");
        if (fireEvent) {
            var event = NativeMobExtensions.targetEvent(mob, target, reason);
            if (event.isCancelled()) return false;
            target = event.getTarget() == null ? null : ((CraftLivingEntity) event.getTarget()).getHandle();
        }
        meldrite$targetDispatch++;
        try { mob.setTarget(target); } finally { meldrite$targetDispatch--; }
        return true;
    }
    @Inject(method = "setTarget(Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private void meldrite$targetEvent(LivingEntity target, CallbackInfo callback) {
        if (meldrite$targetDispatch != 0 || !NativeEntityExtensions.eventsAvailable()) return;
        meldrite$setTarget(target, EntityTargetEvent.TargetReason.UNKNOWN, true);
        callback.cancel();
    }
    @Inject(method = "tickNewAi", at = @At("HEAD"), cancellable = true)
    private void meldrite$awareness(CallbackInfo callback) {
        if (aware) return;
        MobEntity mob = (MobEntity) (Object) this;
        var living = (LivingEntityPaperAccessMixin) this;
        living.meldrite$setDespawnCounter(living.meldrite$getDespawnCounter() + 1);
        for (var goal : goalSelector.getGoals()) {
            if (goal.getGoal() instanceof SwimGoal swim) {
                if (swim.canStart()) swim.tick();
                mob.getJumpControl().tick();
                break;
            }
        }
        if (mob.hurtByWater() && (mob.isTouchingWaterOrRain() || mob.isInsideWaterOrBubbleColumn())) mob.damage(mob.getDamageSources().drown(), 1.0f);
        callback.cancel();
    }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$writeAwareness(NbtCompound nbt, CallbackInfo callback) { if (!aware) nbt.putBoolean("Bukkit.Aware", false); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$readAwareness(NbtCompound nbt, CallbackInfo callback) { aware = !nbt.contains("Bukkit.Aware") || nbt.getBoolean("Bukkit.Aware"); }
}
