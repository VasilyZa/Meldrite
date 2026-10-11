package dev.meldrite.compat.engine;

import dev.meldrite.compat.CompatibilityPack;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PackPlanner {
    public record Plan(String status, List<CompatibilityPack> packs, String reason) {}
    public Plan plan(List<? extends CompatibilityPack> packs) {
        Map<String, CompatibilityPack> unique = new LinkedHashMap<>();
        for (CompatibilityPack pack : packs) if (unique.putIfAbsent(pack.id(), pack) != null) return new Plan("PLAN_REJECTED", List.of(), "CONFLICTING_PACK: " + pack.id());
        return new Plan("APPLY", List.copyOf(unique.values()), "OK");
    }
}
