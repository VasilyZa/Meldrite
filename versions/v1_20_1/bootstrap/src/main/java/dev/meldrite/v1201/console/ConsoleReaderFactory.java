package dev.meldrite.v1201.console;

import java.nio.file.Path;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;

/** Minecraft syntax is passed through unchanged; this console is not a shell. */
public final class ConsoleReaderFactory {
    private ConsoleReaderFactory() {}

    public static LineReader create(Terminal terminal, Path history, Completer completer, Highlighter highlighter) {
        var builder = LineReaderBuilder.builder().terminal(terminal).appName("Meldrite")
                .parser(new DefaultParser().quoteChars(new char[0]).escapeChars(new char[0]))
                .completer(completer).highlighter(highlighter)
                .variable(LineReader.HISTORY_SIZE, 1000).variable(LineReader.HISTORY_FILE_SIZE, 1000)
                .variable(LineReader.BELL_STYLE, "none")
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .option(LineReader.Option.COMPLETE_IN_WORD, true)
                .option(LineReader.Option.INSERT_TAB, false)
                .option(LineReader.Option.AUTO_FRESH_LINE, true)
                .option(LineReader.Option.AUTO_LIST, true).option(LineReader.Option.AUTO_MENU, true)
                .option(LineReader.Option.DISABLE_HIGHLIGHTER, terminal.getType().startsWith("dumb")
                        || "false".equalsIgnoreCase(System.getProperty("terminal.ansi")));
        if (history != null) builder.variable(LineReader.HISTORY_FILE, history);
        var reader = builder.build();
        var keys = reader.getKeyMaps().get(LineReader.MAIN);
        keys.bind(new Reference(LineReader.UP_LINE_OR_HISTORY), "\u001b[A");
        keys.bind(new Reference(LineReader.DOWN_LINE_OR_HISTORY), "\u001b[B");
        keys.bind(new Reference(LineReader.FORWARD_CHAR), "\u001b[C");
        keys.bind(new Reference(LineReader.BACKWARD_CHAR), "\u001b[D");
        keys.bind(new Reference(LineReader.BEGINNING_OF_LINE), "\u001b[H", "\u001b[1~");
        keys.bind(new Reference(LineReader.END_OF_LINE), "\u001b[F", "\u001b[4~");
        return reader;
    }
}
