package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;

public final class CraftObjective implements Objective {
    private final CraftScoreboard board;
    private final ScoreboardObjective handle;
    CraftObjective(CraftScoreboard board, ScoreboardObjective handle) { this.board = board; this.handle = handle; }
    public ScoreboardObjective getHandle() { return handle; }
    CraftScoreboard checkState() {
        if (board.handle.getNullableObjective(handle.getName()) != handle) throw new IllegalStateException("Unregistered scoreboard objective");
        return board;
    }
    @Override public String getName() { checkState(); return handle.getName(); }
    @Override public Component displayName() { checkState(); return ScoreboardText.component(handle.getDisplayName()); }
    @Override public void displayName(Component value) { checkState().mutation(); handle.setDisplayName(ScoreboardText.nativeText(value)); }
    @Override public String getDisplayName() { checkState(); return ScoreboardText.legacy(handle.getDisplayName()); }
    @Override public void setDisplayName(String value) { checkState().mutation(); handle.setDisplayName(ScoreboardText.legacy(value)); }
    @Override public String getCriteria() { checkState(); return handle.getCriterion().getName(); }
    @Override public Criteria getTrackedCriteria() { checkState(); return new CraftCriteria(handle.getCriterion()); }
    @Override public boolean isModifiable() { checkState(); return !handle.getCriterion().isReadOnly(); }
    @Override public CraftScoreboard getScoreboard() { return board; }
    @Override public void unregister() { checkState().mutation(); board.handle.removeObjective(handle); }
    @Override public void setDisplaySlot(DisplaySlot slot) {
        checkState().mutation();
        for (int index = 0; index < net.minecraft.scoreboard.Scoreboard.DISPLAY_SLOT_COUNT; index++) {
            if (board.handle.getObjectiveForSlot(index) == handle) board.handle.setObjectiveSlot(index, null);
        }
        if (slot != null) board.handle.setObjectiveSlot(CraftScoreboard.slot(slot), handle);
    }
    @Override public DisplaySlot getDisplaySlot() {
        checkState();
        for (int index = 0; index < net.minecraft.scoreboard.Scoreboard.DISPLAY_SLOT_COUNT; index++) {
            if (board.handle.getObjectiveForSlot(index) == handle) return CraftScoreboard.slot(index);
        }
        return null;
    }
    @Override public void setRenderType(RenderType value) { checkState().mutation(); handle.setRenderType(net.minecraft.scoreboard.ScoreboardCriterion.RenderType.valueOf(Objects.requireNonNull(value).name())); }
    @Override public RenderType getRenderType() { checkState(); return RenderType.valueOf(handle.getRenderType().name()); }
    @Override public Score getScore(OfflinePlayer player) { return getScore(Objects.requireNonNull(player, "player").getName()); }
    @Override public CraftScore getScore(String entry) { checkState(); return new CraftScore(this, CraftScoreboard.name(entry, "entry")); }
    @Override public Score getScoreFor(Entity entity) { return getScore(CraftScoreboard.entityName(entity)); }
    @Override public boolean equals(Object other) { return other instanceof CraftObjective value && value.handle == handle; }
    @Override public int hashCode() { return System.identityHashCode(handle); }
}
