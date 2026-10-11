package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import com.google.common.collect.ImmutableBiMap;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;

/** Exact display-slot and render-type mappings for the Yarn scoreboard. */
public final class CraftScoreboardTranslations {
    static final int MAX_DISPLAY_SLOT = Scoreboard.getDisplaySlotNames().length;
    @Deprecated static final ImmutableBiMap<DisplaySlot, String> SLOTS = ImmutableBiMap.of(
            DisplaySlot.BELOW_NAME, "belowName", DisplaySlot.PLAYER_LIST, "list", DisplaySlot.SIDEBAR, "sidebar");
    private CraftScoreboardTranslations() { }
    public static DisplaySlot toBukkitSlot(int slot) {
        if (slot < 0 || slot >= MAX_DISPLAY_SLOT) return null;
        String name = Scoreboard.getDisplaySlotName(slot);
        return DisplaySlot.NAMES.value(name);
    }
    public static int fromBukkitSlot(DisplaySlot slot) {
        return Scoreboard.getDisplaySlotId(java.util.Objects.requireNonNull(slot).getId());
    }
    static RenderType toBukkitRender(ScoreboardCriterion.RenderType render) { return RenderType.valueOf(render.name()); }
    static ScoreboardCriterion.RenderType fromBukkitRender(RenderType render) { return ScoreboardCriterion.RenderType.valueOf(render.name()); }
}
