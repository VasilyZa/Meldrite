package dev.meldrite.v1201.console;

import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.*;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.craftbukkit.util.ForwardLogHandler;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/** Adapts the existing logging configuration and leaves file/GUI appenders in place. */
public final class ConsoleLogging {
    // Borrow the descriptor: vanilla may already have wrapped System.out in its own logger.
    private static final PrintStream OUTPUT = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
    private static final String APPENDER = "MeldriteTerminal", ASYNC = "MeldriteAsyncConsole";
    private static volatile Terminal terminal;
    private static volatile LineReader reader;
    private static LoggerContext context;
    private static Configuration configured;
    private static AsyncAppender async;
    private static TerminalAppender destination;
    private static final Map<LoggerConfig, ConsoleRoute> consoleRoutes = new LinkedHashMap<>();
    private static ForwardLogHandler jul;
    private static PrintStream previousOut, previousErr;
    private static PropertyChangeListener reconfigure;
    private ConsoleLogging() {}

    public static synchronized void install() {
        if (context != null) return;
        // Knot's caller loader can create a separate context from Minecraft's shared logging backend.
        context = (LoggerContext) LogManager.getContext(LogManager.class.getClassLoader(), false);
        reconfigure = event -> {
            if ("config".equals(event.getPropertyName()) && event.getNewValue() instanceof Configuration configuration)
                route(configuration);
        };
        context.addPropertyChangeListener(reconfigure);
        route(context.getConfiguration());
        if (jul == null) {
            var root = java.util.logging.Logger.getLogger("");
            for (var handler : root.getHandlers()) if (handler instanceof ConsoleHandler) root.removeHandler(handler);
            // Log4j owns the final level/filter policy; JUL must not discard FINE first.
            root.setLevel(java.util.logging.Level.ALL);
            jul = new ForwardLogHandler(); root.addHandler(jul);
        }
        previousOut = System.out; previousErr = System.err;
        System.setOut(new PrintStream(new LogOutput(false), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new LogOutput(true), true, StandardCharsets.UTF_8));
    }

    private static synchronized void route(Configuration configuration) {
        if (configuration == configured || context == null) return;
        configured = configuration; // updateLoggers also emits a config event.
        consoleRoutes.clear();
        destination = new TerminalAppender(configuration);
        destination.start(); configuration.addAppender(destination);
        async = AsyncAppender.newBuilder().setName(ASYNC).setConfiguration(configuration)
                .setAppenderRefs(new AppenderRef[]{AppenderRef.createAppenderRef(APPENDER, null, null)})
                .setBufferSize(4096).setBlocking(false).setShutdownTimeout(2000).build();
        async.start(); configuration.addAppender(async);
        var loggers = new ArrayList<>(configuration.getLoggers().values());
        if (!loggers.contains(configuration.getRootLogger())) loggers.add(configuration.getRootLogger());
        for (var logger : loggers) {
            for (var appender : List.copyOf(logger.getAppenders().values())) {
                if (!(appender instanceof ConsoleAppender)) continue;
                var reference = logger.getAppenderRefs().stream().filter(ref -> ref.getRef().equals(appender.getName())).findFirst();
                logger.removeAppender(appender.getName());
                Filter referenceFilter = reference.map(AppenderRef::getFilter).orElse(null);
                Filter appenderFilter = ((ConsoleAppender) appender).getFilter();
                Filter filter = referenceFilter == null ? appenderFilter : appenderFilter == null ? referenceFilter
                        : new ConsoleRouteFilter(referenceFilter, appenderFilter);
                var route = new ConsoleRoute(reference.map(AppenderRef::getLevel).orElse(null), filter);
                consoleRoutes.put(logger, route);
                logger.addAppender(async, route.level, route.filter);
            }
        }
        context.updateLoggers();
    }

    public static synchronized Terminal initializeTerminal() {
        if (terminal != null || !ConsoleOptions.interactive()) return terminal;
        try {
            terminal = TerminalBuilder.builder().name("Meldrite").system(true).dumb(false).build();
            if (terminal.getType().startsWith("dumb")) { terminal.close(); terminal = null; }
        } catch (IOException | IllegalStateException unavailable) { terminal = null; }
        return terminal;
    }
    public static void setReader(LineReader value) { reader = value; }

    public static synchronized void close() {
        if (context == null) return;
        context.removePropertyChangeListener(reconfigure);
        reader = null;
        System.out.close(); System.err.close();
        // Keep late shutdown output functional while flushing/stopping the async worker.
        for (var entry : consoleRoutes.entrySet()) {
            var logger = entry.getKey(); var route = entry.getValue();
            logger.removeAppender(ASYNC);
            logger.addAppender(destination, route.level, route.filter);
        }
        context.updateLoggers();
        if (async != null) async.stop(2, TimeUnit.SECONDS);
        var current = terminal; terminal = null;
        if (current != null) try { current.close(); } catch (IOException ignored) { }
        System.setOut(previousOut); System.setErr(previousErr);
        // JUL routing outlives stdin/TTY: plugin shutdown hooks can still log here.
        context = null;
        configured = null;
        consoleRoutes.clear();
    }

    private record ConsoleRoute(org.apache.logging.log4j.Level level, Filter filter) {}

    /** Ref ACCEPT never bypasses the separate appender-level DENY in Log4j. */
    private static final class ConsoleRouteFilter extends AbstractFilter {
        private final Filter reference, appender;
        ConsoleRouteFilter(Filter reference, Filter appender) { this.reference = reference; this.appender = appender; }
        @Override public Result filter(LogEvent event) {
            return reference.filter(event) == Result.DENY || appender.filter(event) == Result.DENY ? Result.DENY : Result.NEUTRAL;
        }
    }

    private static final class TerminalAppender extends AbstractAppender {
        TerminalAppender(Configuration configuration) {
            super(APPENDER, null, PatternLayout.newBuilder().withConfiguration(configuration)
                    .withPattern("[%d{HH:mm:ss}] [%t/%level]: %msg%n%xEx").build(), true, Property.EMPTY_ARRAY);
        }
        @Override public void append(LogEvent event) {
            var currentTerminal = terminal;
            boolean ansi = currentTerminal != null && !"false".equalsIgnoreCase(System.getProperty("terminal.ansi"));
            String text = getLayout().toSerializable(event).toString();
            if (ansi && event.getLevel().isMoreSpecificThan(org.apache.logging.log4j.Level.WARN))
                text = (event.getLevel().isMoreSpecificThan(org.apache.logging.log4j.Level.ERROR) ? "§c" : "§e") + text;
            text = ConsoleText.format(text, ansi);
            var currentReader = reader;
            if (currentReader != null) currentReader.printAbove(text);
            else if (currentTerminal != null) { currentTerminal.writer().print(text); currentTerminal.flush(); }
            else OUTPUT.print(text);
        }
    }

    private static final class LogOutput extends OutputStream {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final boolean error;
        LogOutput(boolean error) { this.error = error; }
        @Override public synchronized void write(int value) {
            if (value == '\n') emit(); else { bytes.write(value); if (bytes.size() >= 8192) emit(); }
        }
        @Override public synchronized void write(byte[] values, int offset, int length) {
            for (int i = offset; i < offset + length; i++) write(values[i] & 255);
        }
        private void emit() {
            if (bytes.size() == 0) return;
            String line = bytes.toString(StandardCharsets.UTF_8); bytes.reset();
            if (line.endsWith("\r")) line = line.substring(0, line.length() - 1);
            var logger = LogManager.getContext(LogManager.class.getClassLoader(), false).getLogger(error ? "STDERR" : "STDOUT");
            if (error) logger.warn(line); else logger.info(line);
        }
        @Override public synchronized void flush() { emit(); }
        @Override public synchronized void close() { emit(); }
    }
}
