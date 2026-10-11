package dev.meldrite.v1201.compat;

import dev.meldrite.v1201.hook.WitchEntityPaperAccessMixin;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.sound.SoundEvents;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;

public final class NativeWitchBehavior {
    private NativeWitchBehavior() {}
    public static net.minecraft.item.ItemStack prepare(WitchEntity witch, net.minecraft.item.ItemStack potion) {
        return CraftItemStack.asNMSCopy(com.destroystokyo.paper.event.entity.WitchReadyPotionEvent.process(
                (org.bukkit.entity.Witch) NativeEntityExtensions.bukkit(witch), CraftItemStack.asCraftMirror(potion)));
    }
    public static java.util.List<net.minecraft.entity.effect.StatusEffectInstance> consume(WitchEntity witch, net.minecraft.item.ItemStack potion) {
        var event = new com.destroystokyo.paper.event.entity.WitchConsumePotionEvent(
                (org.bukkit.entity.Witch) NativeEntityExtensions.bukkit(witch), CraftItemStack.asCraftMirror(potion));
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled() ? java.util.List.of() : net.minecraft.potion.PotionUtil.getPotionEffects(CraftItemStack.asNMSCopy(event.getPotion()));
    }
    public static void drink(WitchEntity witch, org.bukkit.inventory.ItemStack potion) {
        NativeEntityExtensions.requireThread(witch);
        var ready = com.destroystokyo.paper.event.entity.WitchReadyPotionEvent.process(
                (org.bukkit.entity.Witch) NativeEntityExtensions.bukkit(witch), potion);
        witch.equipStack(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(ready));
        ((WitchEntityPaperAccessMixin) witch).meldrite$setDrinkTimeLeft(witch.getMainHandStack().getMaxUseTime());
        witch.setDrinking(true);
        if (!witch.isSilent()) witch.getWorld().playSound(null, witch.getX(), witch.getY(), witch.getZ(), SoundEvents.ENTITY_WITCH_DRINK,
                witch.getSoundCategory(), 1, .8f + witch.getRandom().nextFloat() * .4f);
        var speed = witch.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        var penalty = WitchEntityPaperAccessMixin.meldrite$drinkingSpeedPenalty();
        speed.removeModifier(penalty); speed.addTemporaryModifier(penalty);
    }
    public static void attack(WitchEntity witch, net.minecraft.entity.LivingEntity target) {
        NativeEntityExtensions.requireThread(witch);
        if (witch.isDrinking()) return;
        var velocity = target.getVelocity();
        double dx = target.getX() + velocity.x - witch.getX();
        double dy = target.getEyeY() - 1.100000023841858 - witch.getY();
        double dz = target.getZ() + velocity.z - witch.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        var potion = net.minecraft.potion.Potions.HARMING;
        if (target instanceof net.minecraft.entity.raid.RaiderEntity) {
            potion = target.getHealth() <= 4 ? net.minecraft.potion.Potions.HEALING : net.minecraft.potion.Potions.REGENERATION;
            witch.setTarget(null);
        } else if (distance >= 8 && !target.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.SLOWNESS)) {
            potion = net.minecraft.potion.Potions.SLOWNESS;
        } else if (target.getHealth() >= 8 && !target.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.POISON)) {
            potion = net.minecraft.potion.Potions.POISON;
        } else if (distance <= 3 && !target.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.WEAKNESS) && witch.getRandom().nextFloat() < .25f) {
            potion = net.minecraft.potion.Potions.WEAKNESS;
        }
        var stack = net.minecraft.potion.PotionUtil.setPotion(new net.minecraft.item.ItemStack(net.minecraft.item.Items.SPLASH_POTION), potion);
        var event = new com.destroystokyo.paper.event.entity.WitchThrowPotionEvent((org.bukkit.entity.Witch) NativeEntityExtensions.bukkit(witch),
                (org.bukkit.entity.LivingEntity) NativeEntityExtensions.bukkit(target), CraftItemStack.asCraftMirror(stack));
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        var projectile = new net.minecraft.entity.projectile.thrown.PotionEntity(witch.getWorld(), witch);
        projectile.setItem(CraftItemStack.asNMSCopy(event.getPotion()));
        projectile.setPitch(projectile.getPitch() + 20);
        projectile.setVelocity(dx, dy + distance * .2, dz, .75f, 8);
        if (!witch.isSilent()) witch.getWorld().playSound(null, witch.getX(), witch.getY(), witch.getZ(), SoundEvents.ENTITY_WITCH_THROW,
                witch.getSoundCategory(), 1, .8f + witch.getRandom().nextFloat() * .4f);
        witch.getWorld().spawnEntity(projectile);
    }
}
