package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.entity.LivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class PaperLivingEntityExtensionsMixin implements dev.meldrite.v1201.compat.PaperLivingPickupAccess {
    @org.spongepowered.asm.mixin.Unique private volatile Boolean meldrite$pickupOverride;
    @Override public Boolean meldrite$getPickupOverride() { return meldrite$pickupOverride; }
    @Override public void meldrite$setPickupOverride(Boolean pickup) { meldrite$pickupOverride = pickup; }

    @org.spongepowered.asm.mixin.injection.Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$writePickup(net.minecraft.nbt.NbtCompound nbt, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (meldrite$pickupOverride != null) nbt.putBoolean("Bukkit.CanPickUpLoot", meldrite$pickupOverride);
    }
    @org.spongepowered.asm.mixin.injection.Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$readPickup(net.minecraft.nbt.NbtCompound nbt, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        meldrite$pickupOverride = nbt.contains("Bukkit.CanPickUpLoot", net.minecraft.nbt.NbtElement.NUMBER_TYPE)
                ? nbt.getBoolean("Bukkit.CanPickUpLoot") : null;
    }
    public CraftLivingEntity getBukkitLivingEntity() {
        return (CraftLivingEntity) NativeEntityExtensions.bukkit((LivingEntity) (Object) this);
    }

    @Redirect(method = "damage", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;timeUntilRegen:I", opcode = Opcodes.PUTFIELD))
    private void meldrite$setDamageRegen(LivingEntity entity, int vanillaTicks) {
        entity.timeUntilRegen = ((dev.meldrite.v1201.compat.PaperEntityAccess) (Object) entity).meldrite$getMaximumNoDamageTicks();
    }

    @ModifyConstant(method = "isBlocking", constant = @Constant(intValue = 5), require = 1)
    private int meldrite$shieldDelay(int vanilla) { return getShieldBlockingDelay(); }

    public int getShieldBlockingDelay() { return NativeEntityExtensions.state((LivingEntity) (Object) this).meldrite$getShieldBlockingDelay(); }
    public void setShieldBlockingDelay(int delay) { NativeEntityExtensions.state((LivingEntity) (Object) this).meldrite$setShieldBlockingDelay(delay); }
}
