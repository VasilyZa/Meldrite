package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import java.util.Objects;
import net.minecraft.scoreboard.ScoreboardCriterion;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.RenderType;

public final class CraftCriteria implements Criteria {
    final ScoreboardCriterion handle;
    private final String name;
    CraftCriteria(ScoreboardCriterion handle) { this(handle.getName(), handle); }
    private CraftCriteria(String name, ScoreboardCriterion handle) { this.name = name; this.handle = handle; }
    public static CraftCriteria fromName(String name) {
        if (name == null) throw new IllegalArgumentException("Criteria name cannot be null");
        return ScoreboardCriterion.getOrCreateStatCriterion(name).map(CraftCriteria::new)
                .orElseGet(() -> new CraftCriteria(name, ScoreboardCriterion.DUMMY));
    }
    static ScoreboardCriterion nativeCriteria(Criteria criteria) {
        Objects.requireNonNull(criteria, "criteria");
        return criteria instanceof CraftCriteria nativeValue ? nativeValue.handle : fromName(criteria.getName()).handle;
    }
    @Override public String getName() { return name; }
    @Override public boolean isReadOnly() { return handle.isReadOnly(); }
    @Override public RenderType getDefaultRenderType() { return RenderType.valueOf(handle.getDefaultRenderType().name()); }
    @Override public boolean equals(Object other) { return other instanceof CraftCriteria value && name.equals(value.name); }
    @Override public int hashCode() { return name.hashCode(); }
}
