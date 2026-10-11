package org.bukkit.craftbukkit.v1_20_R1.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import dev.meldrite.v1201.compat.NativeBlockData;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

public final class CraftBanner extends CraftNameableBlockEntityState<BannerBlockEntity> implements Banner {
    public CraftBanner(ServerWorld world, BlockPos position, BlockState state, BannerBlockEntity tile, boolean useSnapshot) {
        super(world, position, state, tile, useSnapshot);
    }

    @Override protected void setNativeCustomName(Text name) { snapshot.setCustomName(name); }
    @Override protected void setLiveCustomName(BannerBlockEntity tile, Text name) { tile.setCustomName(name); }
    @Override public DyeColor getBaseColor() { return DyeColor.valueOf(snapshot.getColorForState().name()); }
    @Override public void setBaseColor(DyeColor color) {
        Objects.requireNonNull(color, "color");
        String suffix = getType().name().endsWith("_WALL_BANNER") ? "_WALL_BANNER" : "_BANNER";
        state = NativeBlockData.block(Material.valueOf(color.name() + suffix)).getStateWithProperties(state);
        snapshot.baseColor = net.minecraft.util.DyeColor.valueOf(color.name());
        snapshot.patterns = null;
    }

    @Override public List<Pattern> getPatterns() {
        NbtList tags = snapshot.createNbt().getList("Patterns", NbtElement.COMPOUND_TYPE);
        List<Pattern> result = new ArrayList<>(tags.size());
        for (int index = 0; index < tags.size(); index++) {
            NbtCompound tag = tags.getCompound(index);
            PatternType type = PatternType.getByIdentifier(tag.getString("Pattern"));
            if (type == null) throw new UnsupportedOperationException("Unmapped native banner pattern: " + tag.getString("Pattern"));
            result.add(new Pattern(DyeColor.getByWoolData((byte) tag.getInt("Color")), type));
        }
        return result;
    }

    @Override public void setPatterns(List<Pattern> patterns) {
        NbtList tags = new NbtList();
        for (Pattern pattern : Objects.requireNonNull(patterns, "patterns")) {
            Objects.requireNonNull(pattern, "pattern");
            NbtCompound tag = new NbtCompound();
            tag.putInt("Color", pattern.getColor().getWoolData());
            tag.putString("Pattern", pattern.getPattern().getIdentifier());
            tags.add(tag);
        }
        NbtCompound nbt = snapshot.createNbt();
        nbt.put("Patterns", tags);
        snapshot.readNbt(nbt);
    }

    @Override public void addPattern(Pattern pattern) { List<Pattern> patterns = getPatterns(); patterns.add(pattern); setPatterns(patterns); }
    @Override public Pattern getPattern(int index) { return getPatterns().get(index); }
    @Override public Pattern removePattern(int index) { List<Pattern> patterns = getPatterns(); Pattern removed = patterns.remove(index); setPatterns(patterns); return removed; }
    @Override public void setPattern(int index, Pattern pattern) { List<Pattern> patterns = getPatterns(); patterns.set(index, pattern); setPatterns(patterns); }
    @Override public int numberOfPatterns() { return snapshot.createNbt().getList("Patterns", NbtElement.COMPOUND_TYPE).size(); }
}
