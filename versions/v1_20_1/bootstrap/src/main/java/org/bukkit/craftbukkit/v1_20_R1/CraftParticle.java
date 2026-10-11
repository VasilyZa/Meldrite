package org.bukkit.craftbukkit.v1_20_R1;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SculkChargeParticleEffect;
import net.minecraft.particle.ShriekParticleEffect;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.PositionSource;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector3f;

/** Maps the Bukkit 1.20.1 particle API to native particle effects. */
public final class CraftParticle {
    private static final Map<String, String> LEGACY_KEYS = Map.ofEntries(
            Map.entry("EXPLOSION_NORMAL", "poof"), Map.entry("EXPLOSION_LARGE", "explosion"),
            Map.entry("EXPLOSION_HUGE", "explosion_emitter"), Map.entry("FIREWORKS_SPARK", "firework"),
            Map.entry("WATER_BUBBLE", "bubble"), Map.entry("WATER_SPLASH", "splash"),
            Map.entry("WATER_WAKE", "fishing"), Map.entry("SUSPENDED", "underwater"),
            Map.entry("SUSPENDED_DEPTH", "underwater"), Map.entry("CRIT_MAGIC", "enchanted_hit"),
            Map.entry("SMOKE_NORMAL", "smoke"), Map.entry("SMOKE_LARGE", "large_smoke"),
            Map.entry("SPELL", "effect"), Map.entry("SPELL_INSTANT", "instant_effect"),
            Map.entry("SPELL_MOB", "entity_effect"), Map.entry("SPELL_MOB_AMBIENT", "ambient_entity_effect"),
            Map.entry("SPELL_WITCH", "witch"), Map.entry("DRIP_WATER", "dripping_water"),
            Map.entry("DRIP_LAVA", "dripping_lava"), Map.entry("VILLAGER_ANGRY", "angry_villager"),
            Map.entry("VILLAGER_HAPPY", "happy_villager"), Map.entry("TOWN_AURA", "mycelium"),
            Map.entry("ENCHANTMENT_TABLE", "enchant"), Map.entry("REDSTONE", "dust"),
            Map.entry("SNOWBALL", "item_snowball"), Map.entry("SNOW_SHOVEL", "item_snowball"),
            Map.entry("SLIME", "item_slime"), Map.entry("ITEM_CRACK", "item"),
            Map.entry("BLOCK_CRACK", "block"), Map.entry("BLOCK_DUST", "block"),
            Map.entry("WATER_DROP", "rain"), Map.entry("MOB_APPEARANCE", "elder_guardian"),
            Map.entry("TOTEM", "totem_of_undying"), Map.entry("FALLING_DUST", "falling_dust"),
            Map.entry("LEGACY_BLOCK_CRACK", "block"), Map.entry("LEGACY_BLOCK_DUST", "block"),
            Map.entry("LEGACY_FALLING_DUST", "falling_dust"));

    private CraftParticle() {}

    public static ParticleEffect toNMS(Particle particle) { return toNMS(particle, null); }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> ParticleEffect toNMS(Particle particle, T data) {
        String key = LEGACY_KEYS.getOrDefault(particle.name(), particle.name().toLowerCase(java.util.Locale.ROOT));
        ParticleType<?> type = Registries.PARTICLE_TYPE.get(Identifier.of("minecraft", key));
        if (type == null) throw new IllegalArgumentException("No native particle for " + particle);
        Class<?> dataType = particle.getDataType();
        if (dataType == Void.class) return (DefaultParticleType) type;
        if (data == null) throw new IllegalArgumentException("Particle " + particle + " requires " + dataType.getName());
        if (dataType == ItemStack.class) return new ItemStackParticleEffect((ParticleType) type, CraftItemStack.asNMSCopy((ItemStack) data));
        if (dataType == BlockData.class) return new BlockStateParticleEffect((ParticleType) type, ((CraftBlockData) data).getState());
        if (dataType == Particle.DustOptions.class) {
            Particle.DustOptions dust = (Particle.DustOptions) data;
            return new DustParticleEffect(color(dust.getColor()), dust.getSize());
        }
        if (dataType == Particle.DustTransition.class) {
            Particle.DustTransition dust = (Particle.DustTransition) data;
            return new DustColorTransitionParticleEffect(color(dust.getColor()), color(dust.getToColor()), dust.getSize());
        }
        if (dataType == Vibration.class) {
            Vibration vibration = (Vibration) data;
            PositionSource source;
            if (vibration.getDestination() instanceof Vibration.Destination.BlockDestination destination) {
                Location location = destination.getLocation();
                source = new BlockPositionSource(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            } else if (vibration.getDestination() instanceof Vibration.Destination.EntityDestination destination) {
                Entity entity = ((CraftEntity) destination.getEntity()).getHandle();
                source = new EntityPositionSource(entity, entity.getStandingEyeHeight());
            } else {
                throw new IllegalArgumentException("Unsupported vibration destination: " + vibration.getDestination());
            }
            return new VibrationParticleEffect(source, vibration.getArrivalTime());
        }
        if (dataType == Float.class) return new SculkChargeParticleEffect((Float) data);
        if (dataType == Integer.class) return new ShriekParticleEffect((Integer) data);
        throw new IllegalArgumentException("Unsupported particle data type " + dataType.getName());
    }

    public static Particle toBukkit(ParticleEffect effect) { return toBukkit(effect.getType()); }

    public static Particle toBukkit(ParticleType<?> type) {
        Identifier key = Registries.PARTICLE_TYPE.getId(type);
        if (key == null) return null;
        String nativeKey = key.getPath();
        for (Map.Entry<String, String> alias : LEGACY_KEYS.entrySet()) {
            if (alias.getValue().equals(nativeKey)) {
                try { return Particle.valueOf(alias.getKey()); } catch (IllegalArgumentException ignored) { }
            }
        }
        try { return Particle.valueOf(nativeKey.toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    private static Vector3f color(org.bukkit.Color color) {
        return new Vector3f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
    }
}
