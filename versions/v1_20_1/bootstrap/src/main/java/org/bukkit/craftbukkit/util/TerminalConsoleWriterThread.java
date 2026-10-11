package org.bukkit.craftbukkit.util;
import com.mojang.logging.LogQueues;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
/** Drains the vanilla terminal queue into a stream for legacy console integrations. */
public final class TerminalConsoleWriterThread extends Thread {
    private final OutputStream output;
    public TerminalConsoleWriterThread(OutputStream output) { super("TerminalConsoleWriter"); this.output = java.util.Objects.requireNonNull(output, "output"); setDaemon(true); }
    public TerminalConsoleWriterThread(OutputStream output, Object ignoredReader) { this(output); }
    @Override public void run() { while (!isInterrupted()) { String message = LogQueues.getNextLogEvent("TerminalConsole"); if (message == null) { Thread.yield(); continue; } try { output.write(message.getBytes(StandardCharsets.UTF_8)); output.flush(); } catch (IOException failure) { interrupt(); return; } } }
}
