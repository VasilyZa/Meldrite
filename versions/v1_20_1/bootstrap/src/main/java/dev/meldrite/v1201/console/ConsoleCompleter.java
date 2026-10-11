package dev.meldrite.v1201.console;

import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Function;
import org.jline.reader.*;

/** Providers run on the server thread, with bounded waits and no abandoned queue entries. */
public final class ConsoleCompleter implements Completer {
    private static final long MAX_WAIT_MILLIS = 100;
    private static final long CACHE_NANOS = TimeUnit.MILLISECONDS.toNanos(250);
    private final Queue<Runnable> tasks;
    private final Function<String, CompletableFuture<Suggestions>> provider;
    private final long timeoutMillis;
    private CachedSuggestions cached;

    public ConsoleCompleter(Queue<Runnable> tasks, Function<String, CompletableFuture<Suggestions>> provider, long timeoutMillis) {
        this.tasks = tasks;
        this.provider = provider;
        // JLine holds its editing lock while invoking the completer. A delayed server
        // must not also freeze editing, log redraws and the terminal's Ctrl+C handler.
        this.timeoutMillis = Math.min(MAX_WAIT_MILLIS, Math.max(1, timeoutMillis));
    }

    @Override public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String input = line.line().substring(0, line.cursor());
        boolean slash = input.startsWith("/");
        String command = slash ? input.substring(1) : input;
        int wordStart = line.cursor() - line.wordCursor() - (slash ? 1 : 0);
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        FutureTask<CompletableFuture<Suggestions>> task = null;
        try {
            var request = cached;
            if (request == null || !request.reusable(command)) {
                task = new FutureTask<>(() -> provider.apply(command));
                tasks.add(task);
                request = new CachedSuggestions(command, task.get(remaining(deadline), TimeUnit.NANOSECONDS));
                cached = request;
            }
            var suggestions = request.value.get(remaining(deadline), TimeUnit.NANOSECONDS);
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
            if (task != null) {
                task.cancel(false);
                tasks.remove(task);
            }
        }
    }

    /** Retains one pending async response so a timeout does not restart slow providers on every Tab. */
    private static final class CachedSuggestions {
        final String command;
        final CompletableFuture<Suggestions> value;
        volatile long completedAt;
        CachedSuggestions(String command, CompletableFuture<Suggestions> suggestions) {
            this.command = command;
            value = suggestions.whenComplete((result, failure) -> completedAt = System.nanoTime());
        }
        boolean reusable(String input) {
            return command.equals(input) && !value.isCompletedExceptionally() && !value.isCancelled()
                    && (!value.isDone() || System.nanoTime() - completedAt < CACHE_NANOS);
        }
    }

    private static long remaining(long deadline) { return Math.max(1, deadline - System.nanoTime()); }
}
