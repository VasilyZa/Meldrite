package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.ItemNbtData;
import java.util.*;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaSuspiciousStew extends CraftMetaItem implements SuspiciousStewMeta {
    public CraftMetaSuspiciousStew() { }
    private CraftMetaSuspiciousStew(CraftMetaSuspiciousStew source) { super(source); }
    private List<Object> entries() { return mutableTag().get("Effects") instanceof List<?> list ? new ArrayList<>(list) : new ArrayList<>(); }
    private static int id(Object entry) { Object id = ItemNbtData.compound(entry).get("EffectId"); return id instanceof Number value ? value.byteValue() & 255 : -1; }
    private void effects(List<Object> effects) { value("Effects", effects.isEmpty() ? null : effects); }
    @Override public boolean hasCustomEffects() { return !entries().isEmpty(); }
    @Override public List<PotionEffect> getCustomEffects() {
        List<PotionEffect> result = new ArrayList<>();
        for (Object entry : entries()) { PotionEffectType type = PotionEffectType.getById(id(entry)); Object duration = ItemNbtData.compound(entry).get("EffectDuration"); if (type != null) result.add(new PotionEffect(type, duration instanceof Number number ? number.intValue() : 160, 0)); }
        return List.copyOf(result);
    }
    @Override public boolean hasCustomEffect(PotionEffectType type) { Objects.requireNonNull(type); return entries().stream().anyMatch(entry -> id(entry) == type.getId()); }
    @Override public boolean addCustomEffect(PotionEffect effect, boolean overwrite) {
        Objects.requireNonNull(effect); List<Object> entries = entries();
        for (int i = 0; i < entries.size(); i++) if (id(entries.get(i)) == effect.getType().getId()) {
            Map<String, Object> old = ItemNbtData.compound(entries.get(i));
            if (!overwrite || old.get("EffectDuration") instanceof Number number && number.intValue() == effect.getDuration()) return false;
            Map<String, Object> next = ItemNbtData.copy(old); next.put("EffectDuration", effect.getDuration()); entries.set(i, next); effects(entries); return true;
        }
        entries.add(new LinkedHashMap<>(Map.of("EffectId", (byte) effect.getType().getId(), "EffectDuration", effect.getDuration()))); effects(entries); return true;
    }
    @Override public boolean removeCustomEffect(PotionEffectType type) { Objects.requireNonNull(type); List<Object> entries = entries(); boolean changed = entries.removeIf(entry -> id(entry) == type.getId()); if (changed) effects(entries); return changed; }
    @Override public boolean clearCustomEffects() { boolean changed = hasCustomEffects(); value("Effects", null); return changed; }
    @Override public CraftMetaSuspiciousStew clone() { return new CraftMetaSuspiciousStew(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = serialized("SUSPICIOUS_STEW"); if (hasCustomEffects()) result.put("effects", getCustomEffects()); return result; }
}
