package dev.meldrite.paper.facade;

import java.util.*;
import org.bukkit.Color;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PotionItemMeta extends org.bukkit.craftbukkit.inventory.CraftMetaItem implements PotionMeta {
    private static final Map<PotionType, String> TYPES = Map.ofEntries(
            Map.entry(PotionType.UNCRAFTABLE, "empty"), Map.entry(PotionType.WATER, "water"), Map.entry(PotionType.MUNDANE, "mundane"), Map.entry(PotionType.THICK, "thick"), Map.entry(PotionType.AWKWARD, "awkward"),
            Map.entry(PotionType.NIGHT_VISION, "night_vision"), Map.entry(PotionType.INVISIBILITY, "invisibility"), Map.entry(PotionType.JUMP, "leaping"), Map.entry(PotionType.FIRE_RESISTANCE, "fire_resistance"), Map.entry(PotionType.SPEED, "swiftness"), Map.entry(PotionType.SLOWNESS, "slowness"),
            Map.entry(PotionType.WATER_BREATHING, "water_breathing"), Map.entry(PotionType.INSTANT_HEAL, "healing"), Map.entry(PotionType.INSTANT_DAMAGE, "harming"), Map.entry(PotionType.POISON, "poison"), Map.entry(PotionType.REGEN, "regeneration"), Map.entry(PotionType.STRENGTH, "strength"),
            Map.entry(PotionType.WEAKNESS, "weakness"), Map.entry(PotionType.LUCK, "luck"), Map.entry(PotionType.TURTLE_MASTER, "turtle_master"), Map.entry(PotionType.SLOW_FALLING, "slow_falling"));
    protected PotionItemMeta() { }
    protected PotionItemMeta(PotionItemMeta source) { super(source); }
    @Override public void setBasePotionData(PotionData data) { Objects.requireNonNull(data); String name = TYPES.get(data.getType()); if (data.isUpgraded()) name = "strong_" + name; else if (data.isExtended()) name = "long_" + name; mutableTag().put("Potion", "minecraft:" + name); }
    @Override public PotionData getBasePotionData() {
        Object raw = mutableTag().get("Potion");
        if (!(raw instanceof String name)) return new PotionData(PotionType.UNCRAFTABLE);
        if (name.startsWith("minecraft:")) name = name.substring(10);
        else if (name.contains(":")) return new PotionData(PotionType.UNCRAFTABLE);
        boolean extended = name.startsWith("long_"); boolean upgraded = name.startsWith("strong_");
        if (extended) name = name.substring(5); else if (upgraded) name = name.substring(7);
        for (var type : TYPES.entrySet()) if (type.getValue().equals(name)) {
            if (extended && !type.getKey().isExtendable() || upgraded && !type.getKey().isUpgradeable()) break;
            return new PotionData(type.getKey(), extended, upgraded);
        }
        return new PotionData(PotionType.UNCRAFTABLE);
    }
    private List<Map<String, Object>> effectTags() { List<Map<String, Object>> result = new ArrayList<>(); if (mutableTag().get("CustomPotionEffects") instanceof List<?> values) for (Object value : values) if (value instanceof Map<?, ?>) result.add(ItemNbtData.compound(value)); return result; }
    private int id(Map<String, Object> value) { return value.get("Id") instanceof Number id ? id.byteValue() & 0xff : -1; }
    private int number(Map<String, Object> value, String key) { return value.get(key) instanceof Number number ? number.intValue() : 0; }
    private boolean flag(Map<String, Object> value, String key, boolean fallback) { return value.get(key) instanceof Number number ? number.byteValue() != 0 : value.get(key) instanceof Boolean flag ? flag : fallback; }
    private void effects(List<Map<String, Object>> values) { if (values.isEmpty()) mutableTag().remove("CustomPotionEffects"); else mutableTag().put("CustomPotionEffects", new ArrayList<>(values)); }
    @Override public boolean hasCustomEffects() { return !effectTags().isEmpty(); }
    @Override public List<PotionEffect> getCustomEffects() {
        List<PotionEffect> result = new ArrayList<>();
        for (Map<String, Object> value : effectTags()) {
            PotionEffectType type = PotionEffectType.getById(id(value));
            if (type == null) throw new IllegalStateException("POTION_EFFECT_TYPE_NOT_REGISTERED: " + id(value));
            boolean particles = flag(value, "ShowParticles", true);
            result.add(new PotionEffect(type, number(value, "Duration"), number(value, "Amplifier"), flag(value, "Ambient", false), particles, flag(value, "ShowIcon", particles)));
        }
        return List.copyOf(result);
    }
    @Override public boolean addCustomEffect(PotionEffect effect, boolean overwrite) {
        Objects.requireNonNull(effect); List<Map<String, Object>> values = effectTags();
        int index = -1; for (int i = 0; i < values.size(); i++) if (id(values.get(i)) == effect.getType().getId()) { index = i; break; }
        if (index >= 0 && !overwrite) return false;
        Map<String, Object> value = index < 0 ? new LinkedHashMap<>() : new LinkedHashMap<>(values.get(index));
        value.put("Id", (byte) effect.getType().getId()); value.put("Amplifier", (byte) effect.getAmplifier()); value.put("Duration", effect.getDuration()); value.put("Ambient", (byte) (effect.isAmbient() ? 1 : 0)); value.put("ShowParticles", (byte) (effect.hasParticles() ? 1 : 0)); value.put("ShowIcon", (byte) (effect.hasIcon() ? 1 : 0));
        if (index >= 0 && value.equals(values.get(index))) return false;
        if (index < 0) values.add(value); else values.set(index, value);
        effects(values); return true;
    }
    @Override public boolean removeCustomEffect(PotionEffectType type) { Objects.requireNonNull(type); List<Map<String, Object>> values = effectTags(); boolean removed = values.removeIf(value -> id(value) == type.getId()); effects(values); return removed; }
    @Override public boolean hasCustomEffect(PotionEffectType type) { Objects.requireNonNull(type); return effectTags().stream().anyMatch(value -> id(value) == type.getId()); }
    @Override public boolean setMainEffect(PotionEffectType type) { Objects.requireNonNull(type); List<Map<String, Object>> values = effectTags(); for (int i = 1; i < values.size(); i++) if (id(values.get(i)) == type.getId()) { Collections.swap(values, 0, i); effects(values); return true; } return false; }
    @Override public boolean clearCustomEffects() { boolean changed = mutableTag().containsKey("CustomPotionEffects"); mutableTag().remove("CustomPotionEffects"); return changed; }
    @Override public boolean hasColor() { return mutableTag().get("CustomPotionColor") instanceof Number; }
    @Override public Color getColor() { return hasColor() ? Color.fromRGB(((Number) mutableTag().get("CustomPotionColor")).intValue()) : null; }
    @Override public void setColor(Color value) { if (value == null) mutableTag().remove("CustomPotionColor"); else mutableTag().put("CustomPotionColor", value.asRGB()); }
    @Override public PotionItemMeta clone() { return new PotionItemMeta(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = super.serialize(); result.put("meta-type", "POTION"); if (mutableTag().containsKey("Potion")) result.put("potion-type", mutableTag().get("Potion")); if (hasColor()) result.put("custom-color", getColor()); if (hasCustomEffects()) result.put("custom-effects", getCustomEffects()); return result; }
}
