package dev.meldrite.paper.facade;

import java.util.*;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkMeta;

/** Firework metadata backed by the 1.20.1 vanilla Fireworks/Explosion tags. */
public class FireworkItemMeta extends org.bukkit.craftbukkit.inventory.CraftMetaItem implements FireworkMeta {
    protected FireworkItemMeta() { }
    protected FireworkItemMeta(FireworkItemMeta source) { super(source); }

    private Map<String, Object> fireworks() { return new LinkedHashMap<>(ItemNbtData.compound(mutableTag().get("Fireworks"))); }
    private List<Map<String, Object>> effectTags() {
        Object values = fireworks().get("Explosions");
        if (!(values instanceof List<?> list)) {
            Object single = mutableTag().get("Explosion");
            if (single instanceof Map<?, ?>) return new ArrayList<>(List.of(ItemNbtData.compound(single)));
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object value : list) if (value instanceof Map<?, ?>) result.add(ItemNbtData.compound(value));
        return result;
    }
    private void setEffectTags(List<Map<String, Object>> values) {
        if (mutableTag().containsKey("Explosion") && !mutableTag().containsKey("Fireworks")) {
            if (values.isEmpty()) mutableTag().remove("Explosion");
            else mutableTag().put("Explosion", new LinkedHashMap<>(values.get(0)));
            return;
        }
        Map<String, Object> root = fireworks();
        if (values.isEmpty()) root.remove("Explosions");
        else root.put("Explosions", new ArrayList<>(values));
        if (root.isEmpty()) mutableTag().remove("Fireworks"); else mutableTag().put("Fireworks", root);
    }
    private static int number(Map<String, Object> tag, String key, int fallback) {
        return tag.get(key) instanceof Number value ? value.intValue() : fallback;
    }
    private static boolean flag(Map<String, Object> tag, String key) {
        Object value = tag.get(key);
        return value instanceof Number number ? number.byteValue() != 0 : value instanceof Boolean flag && flag;
    }
    private static List<Color> colors(Map<String, Object> tag, String key) {
        Object value = tag.get(key);
        List<Color> result = new ArrayList<>();
        if (value instanceof int[] values) for (int color : values) result.add(Color.fromRGB(color));
        else if (value instanceof List<?> values) for (Object color : values) if (color instanceof Number number) result.add(Color.fromRGB(number.intValue()));
        return List.copyOf(result);
    }
    private static int[] colorValues(Collection<Color> colors) { return colors.stream().mapToInt(Color::asRGB).toArray(); }
    private static FireworkEffect.Type type(Object value) {
        int ordinal = value instanceof Number number ? number.intValue() : 0;
        return switch (ordinal) { case 1 -> FireworkEffect.Type.BALL_LARGE; case 2 -> FireworkEffect.Type.STAR; case 3 -> FireworkEffect.Type.CREEPER; case 4 -> FireworkEffect.Type.BURST; default -> FireworkEffect.Type.BALL; };
    }
    public static Map<String, Object> effectTag(FireworkEffect effect) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("Type", (byte) switch (effect.getType()) { case BALL -> 0; case BALL_LARGE -> 1; case STAR -> 2; case CREEPER -> 3; case BURST -> 4; });
        result.put("Flicker", (byte) (effect.hasFlicker() ? 1 : 0));
        result.put("Trail", (byte) (effect.hasTrail() ? 1 : 0));
        result.put("Colors", colorValues(effect.getColors()));
        result.put("FadeColors", colorValues(effect.getFadeColors()));
        return result;
    }
    public static FireworkEffect effect(Map<String, Object> value) {
        FireworkEffect.Builder builder = FireworkEffect.builder().with(type(value.get("Type"))).flicker(flag(value, "Flicker")).trail(flag(value, "Trail"));
        List<Color> colors = colors(value, "Colors");
        List<Color> fades = colors(value, "FadeColors");
        if (!colors.isEmpty()) builder.withColor(colors);
        if (!fades.isEmpty()) builder.withFade(fades);
        return builder.build();
    }
    @Override public void addEffect(FireworkEffect effect) { Objects.requireNonNull(effect, "effect"); List<Map<String, Object>> values = effectTags(); values.add(effectTag(effect)); setEffectTags(values); }
    @Override public void addEffects(FireworkEffect... effects) { Objects.requireNonNull(effects, "effects"); for (FireworkEffect effect : effects) addEffect(effect); }
    @Override public void addEffects(Iterable<FireworkEffect> effects) { Objects.requireNonNull(effects, "effects"); for (FireworkEffect effect : effects) addEffect(effect); }
    @Override public List<FireworkEffect> getEffects() { return List.copyOf(effectTags().stream().map(FireworkItemMeta::effect).toList()); }
    @Override public int getEffectsSize() { return effectTags().size(); }
    @Override public void removeEffect(int index) { List<Map<String, Object>> values = effectTags(); if (index < 0 || index >= values.size()) throw new IndexOutOfBoundsException("Invalid firework effect index " + index); values.remove(index); setEffectTags(values); }
    @Override public void clearEffects() { setEffectTags(new ArrayList<>()); }
    @Override public boolean hasEffects() { return !effectTags().isEmpty(); }
    @Override public int getPower() { return number(fireworks(), "Flight", 0); }
    @Override public void setPower(int power) { if (power < 0 || power > 127) throw new IllegalArgumentException("Power must be between 0 and 127"); Map<String, Object> root = fireworks(); root.put("Flight", (byte) power); mutableTag().put("Fireworks", root); }
    @Override public FireworkItemMeta clone() { return new FireworkItemMeta(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = super.serialize(); result.put("meta-type", "FIREWORK"); if (mutableTag().containsKey("Fireworks")) result.put("fireworks", ItemNbtData.copyValue(mutableTag().get("Fireworks"))); if (hasEffects()) result.put("effects", getEffects()); return result; }
}
