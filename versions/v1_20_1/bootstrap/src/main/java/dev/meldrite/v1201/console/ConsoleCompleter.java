package dev.meldrite.v1201.console;

import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Function;
import org.jline.reader.*;

/** Providers run on the server thread, with bounded waits and no abandoned queue entries. */
public final class ConsoleCompleter implements Completer {
    private final Queue<Runnable> tasks;
    private final Function<String, CompletableFuture<Suggestions>> provider;
    private final long timeoutMillis;

    public ConsoleCompleter(Queue<Runnable> tasks, Function<String, CompletableFuture<Suggestions>> provider, long timeoutMillis) {
        this.tasks = tasks;
        this.provider = provider;
        this.timeoutMillis = timeoutMillis;
    }

    @Override public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String input = line.line().substring(0, line.cursor());
        boolean slash = input.startsWith("/");
        String command = slash ? input.substring(1) : input;
        int wordStart = line.cursor() - line.wordCursor() - (slash ? 1 : 0);
        var task = new FutureTask<>(() -> provider.apply(command));
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        tasks.add(task);
        try {
            var suggestions = task.get(remaining(deadline), TimeUnit.NANOSECONDS)
                    .get(remaining(deadline), TimeUnit.NANOSECONDS);
            suggestions.getList().stream().map(suggestion -> {
                // Brigadier may replace several words (greedy arguments); JLine replaces the current word.
                String applied = suggestion.apply(command);
                if (wordStart < 0) return slash && line.wordIndex() == 0 ? "/" + applied : applied;
                if (applied.length() < wordStart || !applied.regionMatches(0, command, 0, wordStart)) return null;
                String value = applied.substring(wordStart);
                return slash && line.wordIndex() == 0 ? "/" + value : value;
            }).filter(value -> value != null && !value.isEmpty()).distinct().map(Candidate::new).forEach(candidates::add);
        } catch (TimeoutException | ExecutionException | CancellationException ignored) {
            // Slow/unavailable providers must not prevent console input or server startup.
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } finally {
            // Interrupting a running FutureTask here would interrupt the Minecraft server thread.
            task.cancel(false);
            tasks.remove(task);
        }
    }

    private static long remaining(long deadline) { return Math.max(1, deadline - System.nanoTime()); }
}
