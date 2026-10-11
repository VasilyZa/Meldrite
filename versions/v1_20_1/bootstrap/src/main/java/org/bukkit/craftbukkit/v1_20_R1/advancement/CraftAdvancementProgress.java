package org.bukkit.craftbukkit.v1_20_R1.advancement;

import java.util.Collection;
import java.util.Date;
import net.minecraft.advancement.PlayerAdvancementTracker;
import org.bukkit.advancement.Advancement;

public class CraftAdvancementProgress implements org.bukkit.advancement.AdvancementProgress {
    private final CraftAdvancement advancement;
    private final PlayerAdvancementTracker playerData;
    private final net.minecraft.advancement.AdvancementProgress handle;
    public CraftAdvancementProgress(CraftAdvancement advancement, PlayerAdvancementTracker player, net.minecraft.advancement.AdvancementProgress handle) {
        this.advancement = java.util.Objects.requireNonNull(advancement); this.playerData = java.util.Objects.requireNonNull(player); this.handle = java.util.Objects.requireNonNull(handle);
    }
    private void writable() { dev.meldrite.v1201.compat.NativeRecipes.requireThread(); }
    @Override public Advancement getAdvancement() { return advancement; }
    @Override public boolean isDone() { return handle.isDone(); }
    @Override public boolean awardCriteria(String criterion) { writable(); return playerData.grantCriterion(advancement.getHandle(), java.util.Objects.requireNonNull(criterion)); }
    @Override public boolean revokeCriteria(String criterion) { writable(); return playerData.revokeCriterion(advancement.getHandle(), java.util.Objects.requireNonNull(criterion)); }
    @Override public Date getDateAwarded(String criterion) {
        var progress = handle.getCriterionProgress(java.util.Objects.requireNonNull(criterion));
        return progress == null ? null : progress.getObtainedDate();
    }
    private static Collection<String> copy(Iterable<String> criteria) { var result = new java.util.ArrayList<String>(); criteria.forEach(result::add); return java.util.List.copyOf(result); }
    @Override public Collection<String> getRemainingCriteria() { return copy(handle.getUnobtainedCriteria()); }
    @Override public Collection<String> getAwardedCriteria() { return copy(handle.getObtainedCriteria()); }
}
