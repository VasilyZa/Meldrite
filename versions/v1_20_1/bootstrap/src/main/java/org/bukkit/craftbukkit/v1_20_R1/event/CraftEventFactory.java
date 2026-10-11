package org.bukkit.craftbukkit.v1_20_R1.event;

import com.google.common.base.Function;
import dev.meldrite.v1201.compat.NativeDamageEvents;
import java.util.EnumMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

/** Craft damage entry points shared by native hooks and plugins using CraftEventFactory. */
public final class CraftEventFactory {
    public static org.bukkit.block.Block blockDamage;
    public static Entity entityDamage;
    private CraftEventFactory() {}
    public static void callEvent(Event event) { org.bukkit.Bukkit.getPluginManager().callEvent(event); }
    public static EntityDamageEvent handleLivingEntityDamageEvent(Entity entity, DamageSource source, double raw, double hardHatValue, double blockingValue, double armorValue, double resistanceValue, double magicValue, double absorptionValue, Function<Double, Double> hardHat, Function<Double, Double> blocking, Function<Double, Double> armor, Function<Double, Double> resistance, Function<Double, Double> magic, Function<Double, Double> absorption) {
        var values = new EnumMap<DamageModifier, Double>(DamageModifier.class);
        var functions = new EnumMap<DamageModifier, Function<? super Double, Double>>(DamageModifier.class);
        values.put(DamageModifier.BASE, raw); functions.put(DamageModifier.BASE, input -> -0D);
        if (source.isOf(net.minecraft.entity.damage.DamageTypes.FALLING_BLOCK) || source.isOf(net.minecraft.entity.damage.DamageTypes.FALLING_ANVIL)) { values.put(DamageModifier.HARD_HAT, hardHatValue); functions.put(DamageModifier.HARD_HAT, hardHat); }
        if (entity instanceof net.minecraft.entity.player.PlayerEntity) { values.put(DamageModifier.BLOCKING, blockingValue); functions.put(DamageModifier.BLOCKING, blocking); }
        values.put(DamageModifier.ARMOR, armorValue); functions.put(DamageModifier.ARMOR, armor);
        values.put(DamageModifier.RESISTANCE, resistanceValue); functions.put(DamageModifier.RESISTANCE, resistance);
        values.put(DamageModifier.MAGIC, magicValue); functions.put(DamageModifier.MAGIC, magic);
        values.put(DamageModifier.ABSORPTION, absorptionValue); functions.put(DamageModifier.ABSORPTION, absorption);
        return NativeDamageEvents.call(entity, source, values, functions, false);
    }
    public static EntityDamageEvent callNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelled) { return NativeDamageEvents.nonLivingEvent(entity, source, damage, cancelled); }
    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage) { return handleNonLivingEntityDamageEvent(entity, source, damage, true); }
    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelOnZeroDamage) { return handleNonLivingEntityDamageEvent(entity, source, damage, cancelOnZeroDamage, false); }
    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelOnZeroDamage, boolean cancelled) { var event = callNonLivingEntityDamageEvent(entity, source, damage, cancelled); return event.isCancelled() || cancelOnZeroDamage && event.getDamage() == 0; }
}
