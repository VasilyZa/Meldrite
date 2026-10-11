package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.ItemNbtData;
import java.util.*;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.meta.BannerMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaBanner extends CraftMetaItem implements BannerMeta {
    public CraftMetaBanner() { }
    private CraftMetaBanner(CraftMetaBanner source) { super(source); }
    private Map<String, Object> block() { return ItemNbtData.compound(mutableTag().get("BlockEntityTag")); }
    private List<Object> entries() { return block().get("Patterns") instanceof List<?> entries ? new ArrayList<>(entries) : new ArrayList<>(); }
    private static Pattern pattern(Object entry) {
        Map<String, Object> tag = ItemNbtData.compound(entry);
        PatternType type = tag.get("Pattern") instanceof String id ? PatternType.getByIdentifier(id) : null;
        DyeColor color = tag.get("Color") instanceof Number value ? DyeColor.getByWoolData(value.byteValue()) : null;
        return type == null || color == null ? null : new Pattern(color, type);
    }
    private static Map<String, Object> tag(Pattern pattern) { return new LinkedHashMap<>(Map.of("Pattern", pattern.getPattern().getIdentifier(), "Color", (int) pattern.getColor().getWoolData())); }
    private List<Integer> slots(List<Object> entries) { List<Integer> slots = new ArrayList<>(); for (int i = 0; i < entries.size(); i++) if (pattern(entries.get(i)) != null) slots.add(i); return slots; }
    @Override public DyeColor getBaseColor() { return block().get("Base") instanceof Number value ? DyeColor.getByWoolData(value.byteValue()) : null; }
    @Override public void setBaseColor(DyeColor color) { if (color == null) { if (!block().isEmpty()) block().remove("Base"); } else mutableCompound("BlockEntityTag").put("Base", (int) color.getWoolData()); }
    @Override public List<Pattern> getPatterns() {
        List<Pattern> result = new ArrayList<>();
        for (Object entry : entries()) { Pattern pattern = pattern(entry); if (pattern != null) result.add(pattern); }
        return result;
    }
    @Override public void setPatterns(List<Pattern> patterns) {
        Objects.requireNonNull(patterns, "patterns");
        List<Object> entries = new ArrayList<>();
        for (Pattern pattern : patterns) entries.add(tag(pattern));
        mutableCompound("BlockEntityTag").put("Patterns", entries);
    }
    @Override public void addPattern(Pattern pattern) { List<Object> values = entries(); values.add(tag(Objects.requireNonNull(pattern))); mutableCompound("BlockEntityTag").put("Patterns", values); }
    @Override public Pattern getPattern(int index) { return getPatterns().get(index); }
    @Override public Pattern removePattern(int index) { List<Object> values = entries(); int slot = slots(values).get(index); Pattern old = pattern(values.remove(slot)); mutableCompound("BlockEntityTag").put("Patterns", values); return old; }
    @Override public void setPattern(int index, Pattern pattern) { List<Object> values = entries(); int slot = slots(values).get(index); Map<String, Object> entry = ItemNbtData.copy(ItemNbtData.compound(values.get(slot))); entry.putAll(tag(Objects.requireNonNull(pattern))); values.set(slot, entry); mutableCompound("BlockEntityTag").put("Patterns", values); }
    @Override public int numberOfPatterns() { return getPatterns().size(); }
    @Override public CraftMetaBanner clone() { return new CraftMetaBanner(this); }
    @Override public Map<String, Object> serialize() {
        Map<String, Object> result = serialized("BANNER");
        if (getBaseColor() != null) result.put("base-color", getBaseColor().name());
        if (numberOfPatterns() != 0) result.put("patterns", getPatterns().stream().map(pattern -> Map.of("color", pattern.getColor().name(), "pattern", pattern.getPattern().getIdentifier())).toList());
        return result;
    }
}
