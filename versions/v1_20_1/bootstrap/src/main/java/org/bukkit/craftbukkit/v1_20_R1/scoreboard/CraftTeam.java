package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.NameTagVisibility;

public final class CraftTeam implements org.bukkit.scoreboard.Team {
    private final CraftScoreboard board;
    private final Team handle;
    CraftTeam(CraftScoreboard board, Team handle) { this.board = board; this.handle = handle; }
    public Team getHandle() { return handle; }
    private void checkState() { if (board.handle.getTeam(handle.getName()) != handle) throw new IllegalStateException("Unregistered scoreboard team"); }
    private void mutate() { checkState(); board.mutation(); }
    @Override public String getName() { checkState(); return handle.getName(); }
    @Override public Component displayName() { checkState(); return ScoreboardText.component(handle.getDisplayName()); }
    @Override public void displayName(Component value) { mutate(); handle.setDisplayName(ScoreboardText.nativeText(value)); }
    @Override public Component prefix() { checkState(); return ScoreboardText.component(handle.getPrefix()); }
    @Override public void prefix(Component value) { mutate(); handle.setPrefix(ScoreboardText.nativeText(value)); }
    @Override public Component suffix() { checkState(); return ScoreboardText.component(handle.getSuffix()); }
    @Override public void suffix(Component value) { mutate(); handle.setSuffix(ScoreboardText.nativeText(value)); }
    @Override public boolean hasColor() { checkState(); return handle.getColor().isColor(); }
    @Override public TextColor color() {
        checkState(); Integer value = handle.getColor().getColorValue();
        if (value == null) throw new IllegalStateException("Team color has no hex value");
        return NamedTextColor.namedColor(value);
    }
    @Override public void color(NamedTextColor value) { mutate(); handle.setColor(value == null ? Formatting.RESET : Formatting.byName(NamedTextColor.NAMES.key(value))); }
    @Override public String getDisplayName() { checkState(); return ScoreboardText.legacy(handle.getDisplayName()); }
    @Override public void setDisplayName(String value) { mutate(); handle.setDisplayName(ScoreboardText.legacy(value)); }
    @Override public String getPrefix() { checkState(); return ScoreboardText.legacy(handle.getPrefix()); }
    @Override public void setPrefix(String value) { mutate(); handle.setPrefix(ScoreboardText.legacy(value)); }
    @Override public String getSuffix() { checkState(); return ScoreboardText.legacy(handle.getSuffix()); }
    @Override public void setSuffix(String value) { mutate(); handle.setSuffix(ScoreboardText.legacy(value)); }
    @Override public ChatColor getColor() { checkState(); return ChatColor.valueOf(handle.getColor().name()); }
    @Override public void setColor(ChatColor value) {
        mutate(); Objects.requireNonNull(value, "color");
        if (!value.isColor() && value != ChatColor.RESET) throw new IllegalArgumentException("Team color must be a color or RESET");
        handle.setColor(Formatting.valueOf(value.name()));
    }
    @Override public boolean allowFriendlyFire() { checkState(); return handle.isFriendlyFireAllowed(); }
    @Override public void setAllowFriendlyFire(boolean value) { mutate(); handle.setFriendlyFireAllowed(value); }
    @Override public boolean canSeeFriendlyInvisibles() { checkState(); return handle.shouldShowFriendlyInvisibles(); }
    @Override public void setCanSeeFriendlyInvisibles(boolean value) { mutate(); handle.setShowFriendlyInvisibles(value); }
    @Override public NameTagVisibility getNameTagVisibility() { checkState(); return NameTagVisibility.valueOf(handle.getNameTagVisibilityRule().name()); }
    @Override public void setNameTagVisibility(NameTagVisibility value) { mutate(); handle.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.valueOf(Objects.requireNonNull(value).name())); }
    @Override public Set<OfflinePlayer> getPlayers() { return getEntries().stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toUnmodifiableSet()); }
    @Override public Set<String> getEntries() { checkState(); return Set.copyOf(handle.getPlayerList()); }
    @Override public int getSize() { checkState(); return handle.getPlayerList().size(); }
    @Override public CraftScoreboard getScoreboard() { return board; }
    @Override public void addPlayer(OfflinePlayer player) { addEntry(Objects.requireNonNull(player, "player").getName()); }
    @Override public void addEntry(String entry) { mutate(); board.handle.addPlayerToTeam(CraftScoreboard.name(entry, "entry"), handle); }
    @Override public void addEntities(Collection<Entity> entities) { addEntries(Objects.requireNonNull(entities, "entities").stream().map(CraftScoreboard::entityName).toList()); }
    @Override public void addEntries(Collection<String> entries) { mutate(); for (String entry : Objects.requireNonNull(entries, "entries")) addEntry(entry); }
    @Override public boolean removePlayer(OfflinePlayer player) { return removeEntry(Objects.requireNonNull(player, "player").getName()); }
    @Override public boolean removeEntry(String entry) {
        mutate(); CraftScoreboard.name(entry, "entry");
        if (!handle.getPlayerList().contains(entry)) return false;
        board.handle.removePlayerFromTeam(entry, handle); return true;
    }
    @Override public boolean removeEntities(Collection<Entity> entities) { return removeEntries(Objects.requireNonNull(entities, "entities").stream().map(CraftScoreboard::entityName).toList()); }
    @Override public boolean removeEntries(Collection<String> entries) { mutate(); boolean changed = false; for (String entry : Objects.requireNonNull(entries, "entries")) changed |= removeEntry(entry); return changed; }
    @Override public void unregister() { mutate(); board.handle.removeTeam(handle); }
    @Override public boolean hasPlayer(OfflinePlayer player) { return hasEntry(Objects.requireNonNull(player, "player").getName()); }
    @Override public boolean hasEntry(String entry) { checkState(); return handle.getPlayerList().contains(CraftScoreboard.name(entry, "entry")); }
    @Override public OptionStatus getOption(Option option) {
        checkState(); Objects.requireNonNull(option, "option");
        int value = switch (option) {
            case NAME_TAG_VISIBILITY -> handle.getNameTagVisibilityRule().ordinal();
            case DEATH_MESSAGE_VISIBILITY -> handle.getDeathMessageVisibilityRule().ordinal();
            case COLLISION_RULE -> handle.getCollisionRule().ordinal();
        };
        return OptionStatus.values()[value];
    }
    @Override public void setOption(Option option, OptionStatus status) {
        mutate(); Objects.requireNonNull(option, "option"); Objects.requireNonNull(status, "status");
        switch (option) {
            case NAME_TAG_VISIBILITY -> handle.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.values()[status.ordinal()]);
            case DEATH_MESSAGE_VISIBILITY -> handle.setDeathMessageVisibilityRule(AbstractTeam.VisibilityRule.values()[status.ordinal()]);
            case COLLISION_RULE -> handle.setCollisionRule(AbstractTeam.CollisionRule.values()[status.ordinal()]);
        }
    }
    @Override public void addEntity(Entity entity) { addEntry(CraftScoreboard.entityName(entity)); }
    @Override public boolean removeEntity(Entity entity) { return removeEntry(CraftScoreboard.entityName(entity)); }
    @Override public boolean hasEntity(Entity entity) { return hasEntry(CraftScoreboard.entityName(entity)); }
    @Override public boolean equals(Object other) { return other instanceof CraftTeam value && value.handle == handle; }
    @Override public int hashCode() { return System.identityHashCode(handle); }
}
