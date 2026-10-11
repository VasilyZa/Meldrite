package dev.meldrite.v1201.console;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;

/** Reads edited input and hands commands to the normal server command queue. */
public final class ConsoleInputLoop {
    private ConsoleInputLoop() {}
    public static void run(LineReader reader, BooleanSupplier running, Consumer<String> enqueue, Runnable stop) {
        try {
            while (running.getAsBoolean()) {
                String command = reader.readLine("> ").trim();
                if (command.startsWith("/")) command = command.substring(1);
                if (!command.isBlank()) enqueue.accept(command);
            }
        } catch (EndOfFileException closedInput) {
            // Detaching a terminal or closing stdin ends input, not the running server.
        } catch (UserInterruptException interrupted) {
            stop.run();
        } finally {
            try { reader.getHistory().save(); }
            catch (IOException failure) { org.slf4j.LoggerFactory.getLogger("Meldrite").warn("Unable to save console history", failure); }
        }
    }
}
