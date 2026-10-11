package dev.meldrite.v1201.console;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.jline.reader.LineReader;
import org.slf4j.LoggerFactory;

/** Replaces only the dedicated server's stdin reader; command dispatch stays native. */
public final class DedicatedConsole implements AutoCloseable {
    private final MinecraftDedicatedServer server;
    private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private AsyncConsoleHighlighter highlighter;
    private LineReader reader;
    private Thread inputThread;
    private volatile boolean closed;
    public DedicatedConsole(MinecraftDedicatedServer server) { this.server = server; }

    public void start(Thread vanillaInput, Runnable plainInput) {
        if (!ConsoleOptions.enabled()) return;
        var terminal = ConsoleLogging.initializeTerminal();
        if (terminal == null) { plainInput.run(); return; }
        try {
            highlighter = new AsyncConsoleHighlighter(new ConsoleHighlighter(buffer -> {
                if (server.getOverworld() == null) return null;
                var input = new StringReader(buffer);
                if (input.canRead() && input.peek() == '/') input.skip();
                return server.getCommandManager().getDispatcher().parse(input, server.getCommandSource());
            }));
            var completer = new ConsoleCompleter(tasks, command -> {
                if (closed || server.getOverworld() == null) return Suggestions.empty();
                var dispatcher = server.getCommandManager().getDispatcher();
                return dispatcher.getCompletionSuggestions(dispatcher.parse(command, server.getCommandSource()));
            }, 100);
            reader = ConsoleReaderFactory.create(terminal, server.getRunDirectory().toPath().resolve(".console_history"), completer, highlighter);
            ConsoleLogging.setReader(reader);
            var thread = new Thread(() -> {
                try {
                    ConsoleInputLoop.run(reader, () -> !closed && !server.isStopped() && server.isRunning(),
                            command -> server.enqueueCommand(command, server.getCommandSource()), () -> server.stop(false));
                } catch (RuntimeException failure) {
                    LoggerFactory.getLogger("Meldrite").error("Console input failed", failure);
                } finally { ConsoleLogging.setReader(null); }
            }, "Meldrite console input");
            thread.setDaemon(true); thread.setUncaughtExceptionHandler(vanillaInput.getUncaughtExceptionHandler()); thread.start();
            inputThread = thread;
        } catch (RuntimeException unavailable) {
            ConsoleLogging.setReader(null); reader = null; highlighter = null;
            LoggerFactory.getLogger("Meldrite").warn("Unable to initialize interactive console; using plain input", unavailable);
            plainInput.run();
        }
    }

    /** Invoked by the native command tick, on the server thread. */
    public void tick() {
        for (int remaining = 32; remaining > 0; remaining--) {
            var task = tasks.poll(); if (task == null) break; task.run();
        }
        if (highlighter != null) highlighter.update();
    }

    @Override public void close() {
        closed = true;
        Runnable task;
        while ((task = tasks.poll()) != null) if (task instanceof Future<?> pending) pending.cancel(false);
        ConsoleLogging.setReader(null);
        // Interrupt before restoring terminal attributes: a restored TTY can block native reads.
        // The input thread owns history saving; the server never writes that list concurrently.
        if (inputThread != null && inputThread != Thread.currentThread()) {
            inputThread.interrupt();
            try { inputThread.join(1000); }
            catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
        }
        ConsoleLogging.close();
    }
}
