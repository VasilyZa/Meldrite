package dev.meldrite.plugin.spi;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
/** Profile policy is injected once; the planner has no game-version branches. */
public record DependencyRules(CyclePolicy cyclePolicy, OrderingPolicy orderingPolicy, BiPredicate<String,String> versionMatches) {
    public enum CyclePolicy { REJECT, REPORT_AND_BREAK_ORDER }
    @FunctionalInterface public interface OrderingPolicy { List<DependencyEdge> edges(PluginDescriptor plugin,List<PluginDescriptor> plugins); }
    public DependencyRules { Objects.requireNonNull(cyclePolicy);Objects.requireNonNull(orderingPolicy);Objects.requireNonNull(versionMatches); }
}
