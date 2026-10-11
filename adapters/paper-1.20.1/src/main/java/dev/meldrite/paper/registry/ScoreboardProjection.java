package dev.meldrite.paper.registry;

import java.util.Objects;
import java.util.function.Function;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.ScoreboardManager;

/** Version-owned scoreboards enter the API adapter through this binding. */
public final class ScoreboardProjection {
    private static volatile ScoreboardManager manager;
    private static volatile Function<String, Criteria> criteria;
    private ScoreboardProjection() {}

    public static void bind(ScoreboardManager value, Function<String, Criteria> resolver) {
        manager = Objects.requireNonNull(value);
        criteria = Objects.requireNonNull(resolver);
    }

    public static ScoreboardManager manager() {
        ScoreboardManager value = manager;
        if (value == null) throw new IllegalStateException("NATIVE_SCOREBOARD_NOT_BOUND");
        return value;
    }

    public static Criteria criteria(String name) {
        Function<String, Criteria> value = criteria;
        if (value == null) throw new IllegalStateException("NATIVE_SCOREBOARD_NOT_BOUND");
        if (name == null) throw new IllegalArgumentException("Criteria name cannot be null");
        return value.apply(name);
    }
}
