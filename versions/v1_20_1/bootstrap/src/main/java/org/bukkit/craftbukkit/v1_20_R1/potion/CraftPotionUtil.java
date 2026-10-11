package org.bukkit.craftbukkit.v1_20_R1.potion;
import java.util.Map;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
public final class CraftPotionUtil {
    private static final Map<PotionType, String> TYPES = Map.ofEntries(
        Map.entry(PotionType.UNCRAFTABLE, "empty"), Map.entry(PotionType.WATER, "water"), Map.entry(PotionType.MUNDANE, "mundane"), Map.entry(PotionType.THICK, "thick"), Map.entry(PotionType.AWKWARD, "awkward"),
        Map.entry(PotionType.NIGHT_VISION, "night_vision"), Map.entry(PotionType.INVISIBILITY, "invisibility"), Map.entry(PotionType.JUMP, "leaping"), Map.entry(PotionType.FIRE_RESISTANCE, "fire_resistance"), Map.entry(PotionType.SPEED, "swiftness"), Map.entry(PotionType.SLOWNESS, "slowness"),
        Map.entry(PotionType.WATER_BREATHING, "water_breathing"), Map.entry(PotionType.INSTANT_HEAL, "healing"), Map.entry(PotionType.INSTANT_DAMAGE, "harming"), Map.entry(PotionType.POISON, "poison"), Map.entry(PotionType.REGEN, "regeneration"), Map.entry(PotionType.STRENGTH, "strength"),
        Map.entry(PotionType.WEAKNESS, "weakness"), Map.entry(PotionType.LUCK, "luck"), Map.entry(PotionType.TURTLE_MASTER, "turtle_master"), Map.entry(PotionType.SLOW_FALLING, "slow_falling"));
    private CraftPotionUtil() {}
    public static String fromBukkit(PotionData data) { java.util.Objects.requireNonNull(data, "data"); var path = java.util.Objects.requireNonNull(TYPES.get(data.getType()), "potion type"); return "minecraft:" + (data.isUpgraded() ? "strong_" : data.isExtended() ? "long_" : "") + path; }
    public static PotionData toBukkit(String id) {
        if (id == null) return new PotionData(PotionType.UNCRAFTABLE);
        var key = new Identifier(id); if (!key.getNamespace().equals("minecraft")) return new PotionData(PotionType.UNCRAFTABLE);
        String path = key.getPath(); boolean strong = path.startsWith("strong_"), extended = path.startsWith("long_"); if (strong) path = path.substring(7); else if (extended) path = path.substring(5);
        for (var entry : TYPES.entrySet()) if (entry.getValue().equals(path)) { var type = entry.getKey(); if ((extended && !type.isExtendable()) || (strong && !type.isUpgradeable())) break; return new PotionData(type, extended, strong); } return new PotionData(PotionType.UNCRAFTABLE);
    }
    public static StatusEffectInstance fromBukkit(PotionEffect effect) { java.util.Objects.requireNonNull(effect, "effect"); var key = effect.getType().getKey(); var type = Registries.STATUS_EFFECT.getOrEmpty(new Identifier(key.getNamespace(), key.getKey())).orElseThrow(() -> new IllegalArgumentException("Unknown effect: " + key)); return new StatusEffectInstance(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon()); }
    public static PotionEffect toBukkit(StatusEffectInstance effect) { var id = Registries.STATUS_EFFECT.getId(effect.getEffectType()); var type = PotionEffectType.getByKey(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath())); if (type == null) throw new IllegalArgumentException("Unprojected effect: " + id); return new PotionEffect(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()); }
    public static boolean equals(StatusEffect effect, PotionEffectType type) { var key = type.getKey(); return new Identifier(key.getNamespace(), key.getKey()).equals(Registries.STATUS_EFFECT.getId(effect)); }
}
