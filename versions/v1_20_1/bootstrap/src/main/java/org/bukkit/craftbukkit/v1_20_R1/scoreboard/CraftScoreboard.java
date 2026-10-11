package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.minecraft.scoreboard.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

public final class CraftScoreboard implements org.bukkit.scoreboard.Scoreboard {
    final Scoreboard handle;
    private final BooleanSupplier primary;
    public CraftScoreboard(Scoreboard handle, BooleanSupplier primary) { this.handle = Objects.requireNonNull(handle); this.primary = Objects.requireNonNull(primary); }
    public Scoreboard getHandle() { return handle; }
    void mutation() { if (!primary.getAsBoolean()) throw new IllegalStateException("Scoreboard mutation requires the server thread"); }
    static String name(String value, String kind) {
        if (value == null || value.length() > Short.MAX_VALUE) throw new IllegalArgumentException("Invalid " + kind + " name");
        return value;
    }
    static String entityName(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (entity instanceof org.bukkit.entity.Player player) return player.getName();
        return entity.getUniqueId().toString();
    }
    static int slot(DisplaySlot slot) {
        Objects.requireNonNull(slot, "slot");
        return switch (slot) {
            case PLAYER_LIST -> 0;
            case SIDEBAR -> 1;
            case BELOW_NAME -> 2;
            default -> slot.ordinal();
        };
    }
    static DisplaySlot slot(int index) {
        return switch (index) { case 0 -> DisplaySlot.PLAYER_LIST; case 1 -> DisplaySlot.SIDEBAR; case 2 -> DisplaySlot.BELOW_NAME; default -> DisplaySlot.values()[index]; };
    }
    @Override public CraftObjective registerNewObjective(String name, String criteria) { return registerNewObjective(name, criteria, name); }
    @Override public CraftObjective registerNewObjective(String name, String criteria, Component display) { return registerNewObjective(name, criteria, display, RenderType.INTEGER); }
    @Override public CraftObjective registerNewObjective(String name, String criteria, Component display, RenderType render) { return registerNewObjective(name, CraftCriteria.fromName(criteria), display, render); }
    @Override public CraftObjective registerNewObjective(String name, Criteria criteria, Component display) { return registerNewObjective(name, criteria, display, RenderType.INTEGER); }
    @Override public CraftObjective registerNewObjective(String name, Criteria criteria, Component display, RenderType render) {
        mutation(); name(name, "objective"); Objects.requireNonNull(criteria, "criteria"); Objects.requireNonNull(render, "render");
        if (handle.getNullableObjective(name) != null) throw new IllegalArgumentException("Objective already exists: " + name);
        return new CraftObjective(this, handle.addObjective(name, CraftCriteria.nativeCriteria(criteria), ScoreboardText.nativeText(display),
                net.minecraft.scoreboard.ScoreboardCriterion.RenderType.valueOf(render.name())));
    }
    @Override public CraftObjective registerNewObjective(String name, String criteria, String display) { return registerNewObjective(name, criteria, display, RenderType.INTEGER); }
    @Override public CraftObjective registerNewObjective(String name, String criteria, String display, RenderType render) { return registerNewObjective(name, CraftCriteria.fromName(criteria), display, render); }
    @Override public CraftObjective registerNewObjective(String name, Criteria criteria, String display) { return registerNewObjective(name, criteria, display, RenderType.INTEGER); }
    @Override public CraftObjective registerNewObjective(String name, Criteria criteria, String display, RenderType render) { return registerNewObjective(name, criteria, ScoreboardText.component(ScoreboardText.legacy(display)), render); }
    @Override public CraftObjective getObjective(String name) { var value = handle.getNullableObjective(name(name, "objective")); return value == null ? null : new CraftObjective(this, value); }
    @Override public Set<Objective> getObjectivesByCriteria(String criteria) { Objects.requireNonNull(criteria, "criteria"); return getObjectives().stream().filter(value -> value.getCriteria().equals(criteria)).collect(Collectors.toUnmodifiableSet()); }
    @Override public Set<Objective> getObjectivesByCriteria(Criteria criteria) { return getObjectivesByCriteria(Objects.requireNonNull(criteria, "criteria").getName()); }
    @Override public Set<Objective> getObjectives() { return handle.getObjectives().stream().map(value -> (Objective) new CraftObjective(this, value)).collect(Collectors.toUnmodifiableSet()); }
    @Override public CraftObjective getObjective(DisplaySlot slot) { var value = handle.getObjectiveForSlot(slot(slot)); return value == null ? null : new CraftObjective(this, value); }
    @Override public Set<Score> getScores(OfflinePlayer player) { return getScores(Objects.requireNonNull(player, "player").getName()); }
    @Override public Set<Score> getScores(String entry) { name(entry, "entry"); return getObjectives().stream().map(value -> value.getScore(entry)).collect(Collectors.toUnmodifiableSet()); }
    @Override public void resetScores(OfflinePlayer player) { resetScores(Objects.requireNonNull(player, "player").getName()); }
    @Override public void resetScores(String entry) { mutation(); handle.resetPlayerScore(name(entry, "entry"), null); }
    @Override public Team getPlayerTeam(OfflinePlayer player) { return getEntryTeam(Objects.requireNonNull(player, "player").getName()); }
    @Override public CraftTeam getEntryTeam(String entry) { var value = handle.getPlayerTeam(name(entry, "entry")); return value == null ? null : new CraftTeam(this, value); }
    @Override public CraftTeam getTeam(String name) { var value = handle.getTeam(name(name, "team")); return value == null ? null : new CraftTeam(this, value); }
    @Override public Set<Team> getTeams() { return handle.getTeams().stream().map(value -> (Team) new CraftTeam(this, value)).collect(Collectors.toUnmodifiableSet()); }
    @Override public CraftTeam registerNewTeam(String name) { mutation(); name(name, "team"); if (handle.getTeam(name) != null) throw new IllegalArgumentException("Team already exists: " + name); return new CraftTeam(this, handle.addTeam(name)); }
    @Override public Set<OfflinePlayer> getPlayers() { return getEntries().stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toUnmodifiableSet()); }
    @Override public Set<String> getEntries() { return Set.copyOf(handle.getKnownPlayers()); }
    @Override public void clearSlot(DisplaySlot slot) { mutation(); handle.setObjectiveSlot(slot(slot), null); }
    @Override public Set<Score> getScoresFor(Entity entity) { return getScores(entityName(entity)); }
    @Override public void resetScoresFor(Entity entity) { resetScores(entityName(entity)); }
    @Override public Team getEntityTeam(Entity entity) { return getEntryTeam(entityName(entity)); }
}
