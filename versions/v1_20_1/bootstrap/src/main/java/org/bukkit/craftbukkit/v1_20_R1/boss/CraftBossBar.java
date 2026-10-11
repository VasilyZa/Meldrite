package org.bukkit.craftbukkit.v1_20_R1.boss;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.entity.boss.ServerBossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

public class CraftBossBar implements BossBar {
    private final ServerBossBar handle;
    private final Map<BarFlag, FlagContainer> flags;

    public CraftBossBar(String title, BarColor color, BarStyle style, BarFlag... flags) {
        this(new ServerBossBar(CraftChatMessage.fromString(title, true)[0], toNative(color), toNative(style)));
        for (BarFlag flag : Objects.requireNonNull(flags, "flags")) addFlag(flag);
    }
    public CraftBossBar(ServerBossBar handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.flags = new HashMap<>();
        this.flags.put(BarFlag.DARKEN_SKY, new FlagContainer(handle::shouldDarkenSky, handle::setDarkenSky));
        this.flags.put(BarFlag.PLAY_BOSS_MUSIC, new FlagContainer(handle::hasDragonMusic, handle::setDragonMusic));
        this.flags.put(BarFlag.CREATE_FOG, new FlagContainer(handle::shouldThickenFog, handle::setThickenFog));
    }
    public ServerBossBar getHandle() { return handle; }
    protected void requireWritable() {
        if (!org.bukkit.Bukkit.isPrimaryThread()) throw new IllegalStateException("ASYNC_BOSS_BAR_WRITE");
    }
    private static net.minecraft.entity.boss.BossBar.Color toNative(BarColor color) {
        return net.minecraft.entity.boss.BossBar.Color.valueOf(Objects.requireNonNull(color, "color").name());
    }
    private static net.minecraft.entity.boss.BossBar.Style toNative(BarStyle style) {
        Objects.requireNonNull(style, "style");
        return switch (style) {
            case SOLID -> net.minecraft.entity.boss.BossBar.Style.PROGRESS;
            case SEGMENTED_6 -> net.minecraft.entity.boss.BossBar.Style.NOTCHED_6;
            case SEGMENTED_10 -> net.minecraft.entity.boss.BossBar.Style.NOTCHED_10;
            case SEGMENTED_12 -> net.minecraft.entity.boss.BossBar.Style.NOTCHED_12;
            case SEGMENTED_20 -> net.minecraft.entity.boss.BossBar.Style.NOTCHED_20;
        };
    }
    @Override public String getTitle() { return CraftChatMessage.fromComponent(handle.getName()); }
    @Override public void setTitle(String title) { requireWritable(); handle.setName(CraftChatMessage.fromString(title, true)[0]); }
    @Override public BarColor getColor() { return BarColor.valueOf(handle.getColor().name()); }
    @Override public void setColor(BarColor color) { requireWritable(); handle.setColor(toNative(color)); }
    @Override public BarStyle getStyle() {
        return switch (handle.getStyle()) {
            case PROGRESS -> BarStyle.SOLID;
            case NOTCHED_6 -> BarStyle.SEGMENTED_6;
            case NOTCHED_10 -> BarStyle.SEGMENTED_10;
            case NOTCHED_12 -> BarStyle.SEGMENTED_12;
            case NOTCHED_20 -> BarStyle.SEGMENTED_20;
        };
    }
    @Override public void setStyle(BarStyle style) { requireWritable(); handle.setStyle(toNative(style)); }
    @Override public boolean hasFlag(BarFlag flag) {
        FlagContainer container = flags.get(Objects.requireNonNull(flag, "flag"));
        return container != null && container.get.get();
    }
    private void setFlag(BarFlag flag, boolean value) {
        requireWritable();
        FlagContainer container = flags.get(Objects.requireNonNull(flag, "flag"));
        if (container != null) container.set.accept(value);
    }
    @Override public void addFlag(BarFlag flag) { setFlag(flag, true); }
    @Override public void removeFlag(BarFlag flag) { setFlag(flag, false); }
    @Override public double getProgress() { return handle.getPercent(); }
    @Override public void setProgress(double progress) {
        if (!Double.isFinite(progress) || progress < 0 || progress > 1) throw new IllegalArgumentException("Progress must be between 0 and 1");
        requireWritable(); handle.setPercent((float) progress);
    }
    private net.minecraft.server.network.ServerPlayerEntity player(Player player) {
        if (!(player instanceof CraftPlayer craft) || craft.getHandle().networkHandler == null) throw new IllegalArgumentException("Player must be fully connected");
        return craft.getHandle();
    }
    @Override public void addPlayer(Player player) { requireWritable(); handle.addPlayer(player(player)); }
    @Override public void removePlayer(Player player) { requireWritable(); handle.removePlayer(player(player)); }
    @Override public void removeAll() { requireWritable(); handle.clearPlayers(); }
    @Override public List<Player> getPlayers() {
        return handle.getPlayers().stream().map(player -> (Player) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(player)).toList();
    }
    @Override public boolean isVisible() { return handle.isVisible(); }
    @Override public void setVisible(boolean visible) { requireWritable(); handle.setVisible(visible); }
    @Override public void show() { setVisible(true); }
    @Override public void hide() { setVisible(false); }

    /** Binds one Bukkit flag to its live native getter and setter. */
    private final class FlagContainer {
        private final Supplier<Boolean> get;
        private final Consumer<Boolean> set;
        private FlagContainer(Supplier<Boolean> get, Consumer<Boolean> set) {
            this.get = get;
            this.set = set;
        }
    }
}
