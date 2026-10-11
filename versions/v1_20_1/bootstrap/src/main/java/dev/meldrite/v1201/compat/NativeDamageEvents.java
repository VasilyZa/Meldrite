package dev.meldrite.v1201.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.meldrite.v1201.hook.LivingEntityPaperAccessMixin;
import java.util.EnumMap;
import java.util.Map;
import com.google.common.base.Function;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityRegainHealthEvent;

/** Computes events before native damage side effects and carries accepted edits through applyDamage. */
public final class NativeDamageEvents {
    public static final class Damage {
        final LivingEntity entity; final DamageSource source; EntityDamageEvent event;
        Damage(LivingEntity entity, DamageSource source) { this.entity = entity; this.source = source; }
        public EntityDamageEvent event() { return event; }
    }
    private record Preview(LivingEntity entity, boolean magic) {}
    private record Heal(EntityRegainHealthEvent.RegainReason reason, boolean fast) {}
    private static final ThreadLocal<Damage> ACTIVE = new ThreadLocal<>();
    private static final ThreadLocal<Preview> PREVIEW = new ThreadLocal<>();
    private static final ThreadLocal<Heal> HEAL_CONTEXT = new ThreadLocal<>();
    private NativeDamageEvents() {}
    public static boolean available(Entity entity) { return NativeEntityExtensions.eventsAvailable() && !entity.getWorld().isClient && !entity.isRemoved(); }
    public static boolean withBlock(Entity entity, net.minecraft.util.math.BlockPos position, java.util.function.BooleanSupplier action) {
        if (!available(entity)) return action.getAsBoolean();
        var previous = org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.blockDamage;
        var world = NativeEntityExtensions.world((net.minecraft.server.world.ServerWorld) entity.getWorld());
        org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.blockDamage = world.getBlockAt(position.getX(), position.getY(), position.getZ());
        try { return action.getAsBoolean(); } finally { org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.blockDamage = previous; }
    }
    public static Damage active(LivingEntity entity, DamageSource source) { var state = ACTIVE.get(); return state != null && state.event != null && state.entity == entity && state.source == source ? state : null; }
    public static Damage activeFor(LivingEntity entity) { var state = ACTIVE.get(); return state != null && state.event != null && state.entity == entity ? state : null; }
    public static boolean preview(LivingEntity entity) { var preview = PREVIEW.get(); return preview != null && preview.entity == entity; }
    public static boolean previewMagic(LivingEntity entity) { var preview = PREVIEW.get(); return preview != null && preview.entity == entity && preview.magic; }
    public static boolean damage(LivingEntity entity, DamageSource source, float amount, Operation<Boolean> original) {
        var previous = ACTIVE.get(); ACTIVE.set(new Damage(entity, source));
        try { return original.call(source, amount); }
        finally { if (previous == null) ACTIVE.remove(); else ACTIVE.set(previous); }
    }
    public static boolean beforeDamage(LivingEntity entity, DamageSource source, float amount) {
        var frame = ACTIVE.get();
        if (frame == null || frame.entity != entity || frame.source != source || !available(entity)) return true;
        float raw = amount;
        if (source.isIn(DamageTypeTags.IS_FREEZING) && entity.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) raw *= 5;
        var access = (LivingEntityPaperAccessMixin) entity;
        if (entity.timeUntilRegen > NativeEntityExtensions.state(entity).meldrite$getMaximumNoDamageTicks() / 2F && !source.isIn(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (raw <= access.meldrite$getLastDamage()) return false;
            raw -= access.meldrite$getLastDamage();
        }
        var event = living(entity, source, raw);
        if (event.isCancelled() || entity.isRemoved()) return false;
        frame.event = event;
        applyEquipmentDamage(entity, source, event);
        return !(entity instanceof PlayerEntity && event.getFinalDamage() <= 0 && event.getDamage(DamageModifier.BLOCKING) < 0);
    }
    public static EntityDamageEvent living(LivingEntity entity, DamageSource source, double amount) {
        Function<Double, Double> hardHat = input -> source.isIn(DamageTypeTags.DAMAGES_HELMET) && !entity.getEquippedStack(EquipmentSlot.HEAD).isEmpty() ? -input * .25 : -0D;
        Function<Double, Double> blocking = input -> entity.blockedByShield(source) ? -input : -0D;
        Function<Double, Double> armor = input -> (double) armor(entity, source, input.floatValue()) - input;
        Function<Double, Double> resistance = input -> (double) resistance(entity, source, input.floatValue()) - input;
        Function<Double, Double> magic = input -> (double) magic(entity, source, input.floatValue()) - input;
        Function<Double, Double> absorption = input -> -Math.min(Math.max(input, 0), entity.getAbsorptionAmount());
        var values = new EnumMap<DamageModifier, Double>(DamageModifier.class);
        var functions = new EnumMap<DamageModifier, Function<? super Double, Double>>(DamageModifier.class);
        values.put(DamageModifier.BASE, amount); functions.put(DamageModifier.BASE, input -> -0D);
        double remaining = amount;
        if (source.isIn(DamageTypeTags.DAMAGES_HELMET)) remaining = add(values, functions, DamageModifier.HARD_HAT, hardHat, remaining);
        if (entity instanceof PlayerEntity) remaining = add(values, functions, DamageModifier.BLOCKING, blocking, remaining);
        remaining = add(values, functions, DamageModifier.ARMOR, armor, remaining);
        remaining = add(values, functions, DamageModifier.RESISTANCE, resistance, remaining);
        remaining = add(values, functions, DamageModifier.MAGIC, magic, remaining);
        add(values, functions, DamageModifier.ABSORPTION, absorption, remaining);
        return call(entity, source, values, functions, false);
    }
    public static EntityDamageEvent call(Entity entity, DamageSource source, Map<DamageModifier, Double> values, Map<DamageModifier, Function<? super Double, Double>> functions, boolean cancelled) {
        var flags = (PaperDamageSourceAccess) source;
        var api = NativeEntityExtensions.bukkit(entity); var cause = cause(source);
        Entity damager = source.isIndirect() && source.getSource() != null ? source.getSource() : source.getAttacker();
        if (damager == null) damager = source.getSource();
        var block = org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.blockDamage;
        var contextEntity = org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.entityDamage;
        if (damager == null && contextEntity != null) { damager = contextEntity; if (damager instanceof net.minecraft.entity.LightningEntity) cause = DamageCause.LIGHTNING; }
        EntityDamageEvent event;
        if (damager != null) event = new EntityDamageByEntityEvent(NativeEntityExtensions.bukkit(damager), api, cause, values, functions, flags.isCritical());
        else if (block != null || cause == DamageCause.VOID || cause == DamageCause.LAVA || cause == DamageCause.BLOCK_EXPLOSION || blockCause(cause)) event = new EntityDamageByBlockEvent(block, api, cause, values, functions, flags.meldrite$getExplodedBlockState());
        else event = new EntityDamageEvent(api, cause, values, functions);
        event.setCancelled(cancelled);
        org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.blockDamage = null;
        org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.entityDamage = null;
        try { org.bukkit.Bukkit.getPluginManager().callEvent(event); }
        finally { org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.blockDamage = block; org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.entityDamage = contextEntity; }
        var state = NativeEntityExtensions.state(entity); state.meldrite$setLastDamageCancelled(event.isCancelled());
        if (!event.isCancelled()) state.meldrite$setLastDamageCause(event);
        return event;
    }
    public static EntityDamageEvent nonLivingEvent(Entity entity, DamageSource source, double amount, boolean cancelled) {
        var values = new EnumMap<DamageModifier, Double>(DamageModifier.class); values.put(DamageModifier.BASE, amount);
        var functions = new EnumMap<DamageModifier, Function<? super Double, Double>>(DamageModifier.class); functions.put(DamageModifier.BASE, input -> -0D);
        return call(entity, source, values, functions, cancelled);
    }
    public static boolean nonLiving(Entity entity, DamageSource source, float amount, boolean cancelOnZero, boolean cancelled) {
        if (!available(entity)) return false;
        var event = nonLivingEvent(entity, source, amount, cancelled);
        return event.isCancelled() || entity.isRemoved() || cancelOnZero && event.getDamage() == 0;
    }
    private static double add(Map<DamageModifier, Double> values, Map<DamageModifier, Function<? super Double, Double>> functions, DamageModifier modifier, Function<Double, Double> function, double damage) { double value = function.apply(damage); values.put(modifier, value); functions.put(modifier, function); return damage + value; }
    private static float armor(LivingEntity entity, DamageSource source, float amount) {
        var previous = PREVIEW.get(); PREVIEW.set(new Preview(entity, false));
        try { return ((LivingEntityPaperAccessMixin) entity).meldrite$armorDamage(source, amount); }
        finally { if (previous == null) PREVIEW.remove(); else PREVIEW.set(previous); }
    }
    private static float resistance(LivingEntity entity, DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.BYPASSES_EFFECTS) || source.isIn(DamageTypeTags.BYPASSES_RESISTANCE) || !entity.hasStatusEffect(StatusEffects.RESISTANCE)) return amount;
        int reduction = (entity.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
        return Math.max(amount * (25 - reduction) / 25F, 0);
    }
    private static float magic(LivingEntity entity, DamageSource source, float amount) {
        var previous = PREVIEW.get(); PREVIEW.set(new Preview(entity, true));
        try { return ((LivingEntityPaperAccessMixin) entity).meldrite$magicDamage(source, amount); }
        finally { if (previous == null) PREVIEW.remove(); else PREVIEW.set(previous); }
    }
    private static void applyEquipmentDamage(LivingEntity entity, DamageSource source, EntityDamageEvent event) {
        if (source.isIn(DamageTypeTags.DAMAGES_HELMET) && !entity.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) entity.damageHelmet(source, (float) event.getFinalDamage());
        if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) entity.damageArmor(source, (float) (event.getDamage() + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.HARD_HAT)));
        if (event.getDamage(DamageModifier.BLOCKING) < 0) {
            float blocked = (float) -event.getDamage(DamageModifier.BLOCKING);
            entity.getWorld().sendEntityStatus(entity, (byte) 29); entity.damageShield(blocked);
            if (source.getSource() instanceof LivingEntity attacker) ((LivingEntityPaperAccessMixin) entity).meldrite$takeShieldHit(attacker);
            if (entity instanceof ServerPlayerEntity player) player.increaseStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(blocked * 10));
        }
        float resisted = (float) -event.getDamage(DamageModifier.RESISTANCE);
        if (resisted > 0 && resisted < 3.4028235E37F) {
            if (entity instanceof ServerPlayerEntity player) player.increaseStat(Stats.DAMAGE_RESISTED, Math.round(resisted * 10));
            else if (source.getAttacker() instanceof ServerPlayerEntity player) player.increaseStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(resisted * 10));
        }
    }
    public static float armorStage(Damage state) { var event = state.event; return (float) (event.getDamage() + event.getDamage(DamageModifier.HARD_HAT) + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.ARMOR)); }
    public static float magicStage(Damage state) { return (float) Math.max(0, state.event.getFinalDamage() - state.event.getDamage(DamageModifier.ABSORPTION)); }
    public static float absorption(Damage state) { return (float) Math.max(0, -state.event.getDamage(DamageModifier.ABSORPTION)); }
    public static void heal(LivingEntity entity, float amount, EntityRegainHealthEvent.RegainReason reason, boolean fast, Operation<Void> original) {
        if (!available(entity) || !NativeEntityExtensions.state(entity).meldrite$isTracked() || entity.getHealth() <= 0) { original.call(amount); return; }
        var event = new EntityRegainHealthEvent(NativeEntityExtensions.bukkit(entity), amount, reason, fast);
        var previousReason = HEAL_CONTEXT.get(); HEAL_CONTEXT.remove();
        try { org.bukkit.Bukkit.getPluginManager().callEvent(event); }
        finally { if (previousReason != null) HEAL_CONTEXT.set(previousReason); }
        if (!event.isCancelled() && !entity.isRemoved()) original.call((float) event.getAmount());
    }
    public static void withHealReason(EntityRegainHealthEvent.RegainReason reason, Runnable action) { withHealReason(reason, false, action); }
    public static void withHealReason(EntityRegainHealthEvent.RegainReason reason, boolean fast, Runnable action) { var old = HEAL_CONTEXT.get(); HEAL_CONTEXT.set(new Heal(java.util.Objects.requireNonNull(reason), fast)); try { action.run(); } finally { if (old == null) HEAL_CONTEXT.remove(); else HEAL_CONTEXT.set(old); } }
    public static EntityRegainHealthEvent.RegainReason healReason() { var state = HEAL_CONTEXT.get(); return state == null ? EntityRegainHealthEvent.RegainReason.CUSTOM : state.reason; }
    public static boolean fastHeal() { var state = HEAL_CONTEXT.get(); return state != null && state.fast; }
    public static org.bukkit.event.vehicle.VehicleDamageEvent vehicleDamage(Entity entity, DamageSource source, double amount) {
        var event = new org.bukkit.event.vehicle.VehicleDamageEvent((org.bukkit.entity.Vehicle) NativeEntityExtensions.bukkit(entity), source.getAttacker() == null ? null : NativeEntityExtensions.bukkit(source.getAttacker()), amount);
        org.bukkit.Bukkit.getPluginManager().callEvent(event); return event;
    }
    public static boolean vehicleDestroy(Entity entity, DamageSource source) {
        var event = new org.bukkit.event.vehicle.VehicleDestroyEvent((org.bukkit.entity.Vehicle) NativeEntityExtensions.bukkit(entity), source.getAttacker() == null ? null : NativeEntityExtensions.bukkit(source.getAttacker()));
        org.bukkit.Bukkit.getPluginManager().callEvent(event); return !event.isCancelled();
    }
    private static boolean blockCause(DamageCause cause) { return cause == DamageCause.CONTACT || cause == DamageCause.HOT_FLOOR || cause == DamageCause.FIRE; }
    public static DamageCause cause(DamageSource source) {
        var flags = (PaperDamageSourceAccess) source;
        if (source.isIn(DamageTypeTags.IS_EXPLOSION)) return source.getAttacker() == null || source.getAttacker() instanceof net.minecraft.entity.TntEntity ? DamageCause.BLOCK_EXPLOSION : DamageCause.ENTITY_EXPLOSION;
        if (source.getAttacker() != null || source.getSource() != null) {
            if (source.isOf(DamageTypes.THORNS)) return DamageCause.THORNS;
            if (source.isOf(DamageTypes.SONIC_BOOM)) return DamageCause.SONIC_BOOM;
            if (source.isOf(DamageTypes.FALLING_BLOCK) || source.isOf(DamageTypes.FALLING_ANVIL) || source.isOf(DamageTypes.FALLING_STALACTITE)) return DamageCause.FALLING_BLOCK;
            if (source.getSource() instanceof net.minecraft.entity.projectile.thrown.PotionEntity) return DamageCause.MAGIC;
            if (source.getSource() instanceof net.minecraft.entity.projectile.ProjectileEntity) return DamageCause.PROJECTILE;
            if (source.isOf(DamageTypes.LIGHTNING_BOLT)) return DamageCause.LIGHTNING;
            return flags.isSweep() ? DamageCause.ENTITY_SWEEP_ATTACK : DamageCause.ENTITY_ATTACK;
        }
        if (flags.isMelting()) return DamageCause.MELTING;
        if (flags.isPoison()) return DamageCause.POISON;
        if (source.isOf(DamageTypes.OUT_OF_WORLD)) return DamageCause.VOID;
        if (source.isOf(DamageTypes.LAVA)) return DamageCause.LAVA;
        if (source.isOf(DamageTypes.CACTUS) || source.isOf(DamageTypes.SWEET_BERRY_BUSH) || source.isOf(DamageTypes.STALAGMITE)) return DamageCause.CONTACT;
        if (source.isOf(DamageTypes.HOT_FLOOR)) return DamageCause.HOT_FLOOR;
        if (source.isOf(DamageTypes.IN_FIRE)) return DamageCause.FIRE;
        if (source.isOf(DamageTypes.ON_FIRE)) return DamageCause.FIRE_TICK;
        if (source.isOf(DamageTypes.STARVE)) return DamageCause.STARVATION;
        if (source.isOf(DamageTypes.WITHER)) return DamageCause.WITHER;
        if (source.isOf(DamageTypes.IN_WALL)) return DamageCause.SUFFOCATION;
        if (source.isOf(DamageTypes.DROWN)) return DamageCause.DROWNING;
        if (source.isOf(DamageTypes.FREEZE)) return DamageCause.FREEZE;
        if (source.isOf(DamageTypes.FALL)) return DamageCause.FALL;
        if (source.isOf(DamageTypes.FLY_INTO_WALL)) return DamageCause.FLY_INTO_WALL;
        if (source.isOf(DamageTypes.CRAMMING)) return DamageCause.CRAMMING;
        if (source.isOf(DamageTypes.DRY_OUT)) return DamageCause.DRYOUT;
        if (source.isOf(DamageTypes.MAGIC)) return DamageCause.MAGIC;
        if (source.isOf(DamageTypes.LIGHTNING_BOLT)) return DamageCause.LIGHTNING;
        if (source.isOf(DamageTypes.DRAGON_BREATH)) return DamageCause.DRAGON_BREATH;
        if (source.isOf(DamageTypes.GENERIC_KILL)) return DamageCause.KILL;
        if (source.isOf(DamageTypes.OUTSIDE_BORDER)) return DamageCause.WORLD_BORDER;
        return DamageCause.CUSTOM;
    }
}
