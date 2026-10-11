package org.bukkit.craftbukkit.util;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/** Bridges JUL records into Log4j without duplicate console output. */
public final class ForwardLogHandler extends Handler {
    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();
    private final org.apache.logging.log4j.spi.LoggerContext context = LogManager.getContext(LogManager.class.getClassLoader(), false);
    public ForwardLogHandler() {
        setLevel(Level.ALL);
        // Only formatMessage is used: retain JUL bundles/parameters, without dates or source names.
        setFormatter(new SimpleFormatter());
    }
    @Override public void publish(LogRecord record) {
        if (record == null || !isLoggable(record)) return;
        Logger logger = loggers.computeIfAbsent(String.valueOf(record.getLoggerName()), context::getLogger);
        String message = getFormatter() == null ? record.getMessage() : getFormatter().formatMessage(record);
        Throwable throwable = record.getThrown(); Level level = record.getLevel();
        if (level.intValue() >= Level.SEVERE.intValue()) logger.error(message, throwable); else if (level.intValue() >= Level.WARNING.intValue()) logger.warn(message, throwable);
        else if (level.intValue() >= Level.INFO.intValue()) logger.info(message, throwable); else if (level.intValue() >= Level.FINE.intValue()) logger.debug(message, throwable); else logger.trace(message, throwable);
    }
    @Override public void flush() {}
    @Override public void close() throws SecurityException {}
}
