package org.bukkit.craftbukkit.inventory;

import java.util.Map;
import java.util.Objects;
import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaTropicalFishBucket extends CraftMetaItem implements TropicalFishBucketMeta {
    public CraftMetaTropicalFishBucket() { }
    private CraftMetaTropicalFishBucket(CraftMetaTropicalFishBucket source) { super(source); }
    private int variant() { if (!hasVariant()) throw new IllegalStateException("Tropical fish variant is absent"); return number("BucketVariantTag", 0); }
    private void initialize() { if (!hasVariant()) value("BucketVariantTag", 0); }
    @Override public boolean hasVariant() { return mutableTag().get("BucketVariantTag") instanceof Number; }
    @Override public DyeColor getPatternColor() { return DyeColor.getByWoolData((byte) (variant() >>> 24)); }
    @Override public DyeColor getBodyColor() { return DyeColor.getByWoolData((byte) (variant() >>> 16)); }
    @Override public TropicalFish.Pattern getPattern() { int data = variant() & 0xffff; int size = data & 0xff; int pattern = data >>> 8; return size <= 1 && pattern < 6 ? TropicalFish.Pattern.values()[pattern + size * 6] : null; }
    @Override public void setPatternColor(DyeColor color) { initialize(); value("BucketVariantTag", variant() & 0x00ffffff | Objects.requireNonNull(color).getWoolData() << 24); }
    @Override public void setBodyColor(DyeColor color) { initialize(); value("BucketVariantTag", variant() & 0xff00ffff | Objects.requireNonNull(color).getWoolData() << 16); }
    @Override public void setPattern(TropicalFish.Pattern pattern) { initialize(); int ordinal = Objects.requireNonNull(pattern).ordinal(); value("BucketVariantTag", variant() & 0xffff0000 | ordinal % 6 << 8 | ordinal / 6); }
    @Override public CraftMetaTropicalFishBucket clone() { return new CraftMetaTropicalFishBucket(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = serialized("TROPICAL_FISH_BUCKET"); if (hasVariant()) result.put("fish-variant", number("BucketVariantTag", 0)); return result; }
}
