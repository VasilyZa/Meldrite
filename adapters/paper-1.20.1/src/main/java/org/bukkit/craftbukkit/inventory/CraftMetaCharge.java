package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.FireworkItemMeta;
import dev.meldrite.paper.facade.ItemNbtData;
import java.util.Map;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaCharge extends CraftMetaItem implements FireworkEffectMeta {
    public CraftMetaCharge() { }
    private CraftMetaCharge(CraftMetaCharge source) { super(source); }
    @Override public boolean hasEffect() { return mutableTag().get("Explosion") instanceof Map<?, ?>; }
    @Override public FireworkEffect getEffect() { return hasEffect() ? FireworkItemMeta.effect(ItemNbtData.compound(mutableTag().get("Explosion"))) : null; }
    @Override public void setEffect(FireworkEffect effect) {
        if (effect == null) { value("Explosion", null); return; }
        Map<String, Object> explosion = ItemNbtData.copy(ItemNbtData.compound(mutableTag().get("Explosion")));
        explosion.putAll(FireworkItemMeta.effectTag(effect)); value("Explosion", explosion);
    }
    @Override public CraftMetaCharge clone() { return new CraftMetaCharge(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = serialized("FIREWORK_EFFECT"); if (hasEffect()) result.put("firework-effect", getEffect()); return result; }
}
