package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.TropicalFishEntityPaperAccessMixin;
import net.minecraft.entity.passive.TropicalFishEntity;
import org.bukkit.DyeColor;

public class CraftTropicalFish extends io.papermc.paper.entity.PaperSchoolableFish implements org.bukkit.entity.TropicalFish {
    public CraftTropicalFish(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public TropicalFishEntity getHandle() { return (TropicalFishEntity) super.getHandle(); }
    @Override public DyeColor getPatternColor() { return DyeColor.valueOf(getHandle().getPatternColorComponents().name()); }
    @Override public DyeColor getBodyColor() { return DyeColor.valueOf(getHandle().getBaseColorComponents().name()); }
    @Override public Pattern getPattern() { return Pattern.valueOf(getHandle().getVariant().name()); }
    @Override public void setPatternColor(DyeColor color) { setData(getData(java.util.Objects.requireNonNull(color, "color"), getBodyColor(), getPattern())); }
    @Override public void setBodyColor(DyeColor color) { setData(getData(getPatternColor(), java.util.Objects.requireNonNull(color, "color"), getPattern())); }
    @Override public void setPattern(Pattern pattern) { setData(getData(getPatternColor(), getBodyColor(), java.util.Objects.requireNonNull(pattern, "pattern"))); }
    private void setData(int data) { requireWritable(); ((TropicalFishEntityPaperAccessMixin) getHandle()).meldrite$setVariant(data); }
    public enum CraftPattern {
        KOB(0, false), SUNSTREAK(1, false), SNOOPER(2, false), DASHER(3, false), BRINELY(4, false), SPOTTY(5, false),
        FLOPPER(0, true), STRIPEY(1, true), GLITTER(2, true), BLOCKFISH(3, true), BETTY(4, true), CLAYFISH(5, true);
        private final int data;
        CraftPattern(int pattern, boolean large) { data = pattern << 8 | (large ? 1 : 0); }
        public int getDataValue() { return data; }
        public static Pattern fromData(int data) { for (CraftPattern pattern : values()) if (pattern.data == data) return Pattern.valueOf(pattern.name()); return null; }
    }
    public static int getData(DyeColor patternColor, DyeColor bodyColor, Pattern pattern) { return patternColor.getWoolData() << 24 | bodyColor.getWoolData() << 16 | CraftPattern.valueOf(pattern.name()).getDataValue(); }
    public static DyeColor getPatternColor(int data) { return DyeColor.getByWoolData((byte) (data >>> 24)); }
    public static DyeColor getBodyColor(int data) { return DyeColor.getByWoolData((byte) (data >>> 16)); }
    public static Pattern getPattern(int data) { return CraftPattern.fromData(data & 0xFFFF); }
}
