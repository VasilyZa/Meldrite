package dev.meldrite.v1201.hook;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityStateAccessor {
    @Accessor("lastDamageTaken") float meldrite$getLastDamage();
    @Accessor("lastDamageTaken") void meldrite$setLastDamage(float damage);
    @Accessor("despawnCounter") int meldrite$getDespawnCounter();
    @Accessor("despawnCounter") void meldrite$setDespawnCounter(int ticks);
    @Accessor("attackingPlayer") PlayerEntity meldrite$getAttackingPlayer();
    @Accessor("attackingPlayer") void meldrite$setAttackingPlayer(PlayerEntity player);
    @Accessor("jumping") boolean meldrite$isJumping();
    @Invoker("getHurtSound") SoundEvent meldrite$getHurtSound(DamageSource source);
    @Invoker("getDeathSound") SoundEvent meldrite$getDeathSound();
    @Invoker("getFallSound") SoundEvent meldrite$getFallSound(int fallHeight);
    @Invoker("getDrinkSound") SoundEvent meldrite$getDrinkSound(ItemStack stack);
    @Invoker("getEatSound") SoundEvent meldrite$getEatSound(ItemStack stack);
    @Invoker("setLivingFlag") void meldrite$setLivingFlag(int mask, boolean enabled);
    @Invoker("sendEquipmentChanges") void meldrite$sendEquipmentChanges();
}
