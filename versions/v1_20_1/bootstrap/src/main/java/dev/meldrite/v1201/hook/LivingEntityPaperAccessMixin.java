package dev.meldrite.v1201.hook;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(LivingEntity.class)
public interface LivingEntityPaperAccessMixin {
 @Accessor("lastDamageTaken") float meldrite$getLastDamage(); @Accessor("lastDamageTaken") void meldrite$setLastDamage(float value);
 @Accessor("despawnCounter") int meldrite$getDespawnCounter(); @Accessor("despawnCounter") void meldrite$setDespawnCounter(int value);
 @Accessor("attackingPlayer") PlayerEntity meldrite$getAttackingPlayer(); @Accessor("attackingPlayer") void meldrite$setAttackingPlayer(PlayerEntity value);
 @Accessor("jumping") boolean meldrite$isJumping();
 @Invoker("getHurtSound") SoundEvent meldrite$getHurtSound(DamageSource source); @Invoker("getDeathSound") SoundEvent meldrite$getDeathSound();
 @Invoker("getFallSound") SoundEvent meldrite$getFallSound(int value); @Invoker("getDrinkSound") SoundEvent meldrite$getDrinkSound(ItemStack value); @Invoker("getEatSound") SoundEvent meldrite$getEatSound(ItemStack value);
 @Invoker("setLivingFlag") void meldrite$setLivingFlag(int mask, boolean enabled); @Invoker("sendEquipmentChanges") void meldrite$sendEquipmentChanges();
 @Invoker("applyArmorToDamage") float meldrite$armorDamage(DamageSource source, float amount);
 @Invoker("modifyAppliedDamage") float meldrite$magicDamage(DamageSource source, float amount);
 @Invoker("applyDamage") void meldrite$applyDamage(DamageSource source, float amount);
 @Invoker("takeShieldHit") void meldrite$takeShieldHit(LivingEntity attacker);
}
