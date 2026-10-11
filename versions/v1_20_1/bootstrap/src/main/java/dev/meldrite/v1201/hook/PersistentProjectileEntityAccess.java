package dev.meldrite.v1201.hook;

import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PersistentProjectileEntity.class)
public interface PersistentProjectileEntityAccess {
    @Accessor("inGround") boolean meldrite$isInGround();
    @Accessor("inGround") void meldrite$setInGround(boolean value);
    @Accessor("inBlockState") BlockState meldrite$getInBlockState();
    @Accessor("pickupType") PersistentProjectileEntity.PickupPermission meldrite$getPickupType();
    @Accessor("pickupType") void meldrite$setPickupType(PersistentProjectileEntity.PickupPermission value);
    @Accessor("life") int meldrite$getLife();
    @Accessor("life") void meldrite$setLife(int value);
    @Accessor("sound") SoundEvent meldrite$getSound();
    @Accessor("sound") void meldrite$setSound(SoundEvent value);
    @Invoker("asItemStack") ItemStack meldrite$asItemStack();
    @Invoker("getHitSound") SoundEvent meldrite$getHitSound();
}
