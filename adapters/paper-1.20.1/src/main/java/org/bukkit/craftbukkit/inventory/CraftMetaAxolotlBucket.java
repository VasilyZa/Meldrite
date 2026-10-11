package org.bukkit.craftbukkit.inventory;

import java.util.Map;
import java.util.Objects;
import org.bukkit.entity.Axolotl;
import org.bukkit.inventory.meta.AxolotlBucketMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaAxolotlBucket extends CraftMetaItem implements AxolotlBucketMeta {
    public CraftMetaAxolotlBucket() { }
    private CraftMetaAxolotlBucket(CraftMetaAxolotlBucket source) { super(source); }
    @Override public boolean hasVariant() { return mutableTag().get("Variant") instanceof Number; }
    @Override public Axolotl.Variant getVariant() { if (!hasVariant()) throw new IllegalStateException("Axolotl variant is absent"); int variant = number("Variant", 0); if (variant < 0 || variant >= Axolotl.Variant.values().length) throw new IllegalStateException("Unknown axolotl variant " + variant); return Axolotl.Variant.values()[variant]; }
    @Override public void setVariant(Axolotl.Variant variant) { value("Variant", Objects.requireNonNull(variant, "variant").ordinal()); }
    @Override public CraftMetaAxolotlBucket clone() { return new CraftMetaAxolotlBucket(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = serialized("AXOLOTL_BUCKET"); if (hasVariant()) result.put("axolotl-variant", number("Variant", 0)); return result; }
}
