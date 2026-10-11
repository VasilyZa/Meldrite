package dev.meldrite.paper.facade;

import java.util.Arrays;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.SpawnEggMeta;

public class SpawnEggItemMeta extends org.bukkit.craftbukkit.inventory.CraftMetaItem implements SpawnEggMeta {
    private EntityType legacyType;
    private EntityType spawnedType;
    protected SpawnEggItemMeta() {}
    protected SpawnEggItemMeta(EntityType type) { spawnedType = type; }
    protected SpawnEggItemMeta(SpawnEggItemMeta source) { super(source); legacyType = source.legacyType; spawnedType = source.spawnedType; }
    @Override public EntityType getSpawnedType() { return spawnedType; }
    @Override public void setSpawnedType(EntityType type) { spawnedType = type; }
    @Override public EntityType getCustomSpawnedType() {
        Object id = ItemNbtData.compound(mutableTag().get("EntityTag")).get("id");
        if (!(id instanceof String text)) return null;
        return Arrays.stream(EntityType.values()).filter(type -> type != EntityType.UNKNOWN && type.getKey().toString().equals(text)).findFirst().orElse(null);
    }
    @Override public void setCustomSpawnedType(EntityType type) {
        Map<String, Object> values = ItemNbtData.copy(ItemNbtData.compound(mutableTag().get("EntityTag")));
        if (type == null) values.remove("id"); else values.put("id", type.getKey().toString());
        if (!values.isEmpty() || mutableTag().containsKey("EntityTag")) mutableTag().put("EntityTag", values);
    }
    Material updateMaterial(Material material) {
        if (spawnedType != null) return new ItemFactoryAdapter().getSpawnEgg(spawnedType);
        if (legacyType == null) { legacyType = EntityType.fromId(getDamage()); setDamage(0); }
        if (legacyType == null) return material;
        setCustomSpawnedType(null);
        return ItemFactoryAdapter.modernMaterial(Material.LEGACY_MONSTER_EGG, (short) legacyType.getTypeId());
    }
    @Override public SpawnEggItemMeta clone() { return new SpawnEggItemMeta(this); }
    @Override public Map<String, Object> serialize() {
        Map<String, Object> values = super.serialize(); values.put("meta-type", "SPAWN_EGG");
        if (spawnedType != null) values.put("spawned-type", spawnedType.name());
        return values;
    }
}
