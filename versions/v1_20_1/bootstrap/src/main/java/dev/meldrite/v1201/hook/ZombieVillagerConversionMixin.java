package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeVillagerConversionState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ZombieVillagerEntity.class)
public abstract class ZombieVillagerConversionMixin implements NativeVillagerConversionState {
    @Unique private boolean meldrite$broadcastConversion = true;
    @Override public void meldrite$setBroadcastConversion(boolean value) { meldrite$broadcastConversion = value; }
    @WrapOperation(method = "setConverting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;sendEntityStatus(Lnet/minecraft/entity/Entity;B)V"))
    private void meldrite$conversionSound(World world, Entity entity, byte status, Operation<Void> original) { if (meldrite$broadcastConversion) original.call(world, entity, status); }
}
