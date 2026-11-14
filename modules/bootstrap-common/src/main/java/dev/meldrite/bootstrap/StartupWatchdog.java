package dev.meldrite.bootstrap;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Bounded startup diagnostics. The observer never queries or mutates a live world. */
public final class StartupWatchdog implements AutoCloseable {
    private final Thread serverThread;
    private final Consumer<String> output;
    private final ScheduledExecutorService observer;
    private final long started = System.nanoTime();
    private volatile String stage = "Meldrite bootstrap";
    private volatile boolean closed;

    public StartupWatchdog(Thread serverThread, Consumer<String> output) {
        this(serverThread, output, Duration.ofSeconds(30));
    }

    public StartupWatchdog(Thread serverThread, Consumer<String> output, Duration interval) {
        this.serverThread = Objects.requireNonNull(serverThread);
        this.output = Objects.requireNonNull(output);
        long delay = Objects.requireNonNull(interval).toNanos();
        if (delay <= 0) throw new IllegalArgumentException("Startup diagnostic interval must be positive");
        observer = Executors.newSingleThreadScheduledExecutor(operation -> {
            var thread = new Thread(operation, "Meldrite startup observer");
            thread.setDaemon(true);
            thread.setContextClassLoader(StartupWatchdog.class.getClassLoader());
            return thread;
        });
        observer.scheduleWithFixedDelay(this::report, delay, delay, TimeUnit.NANOSECONDS);
    }

    public void stage(String stage) { this.stage = Objects.requireNonNull(stage); }

    private void report() {
        if (closed) return;
        try {
            var heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            var operatingSystem = ManagementFactory.getOperatingSystemMXBean();
            String memory = "heap MiB=" + mib(heap.getUsed()) + "/" + mib(heap.getMax());
            if (operatingSystem instanceof com.sun.management.OperatingSystemMXBean system) {
                memory += ", OS free/total MiB=" + mib(system.getFreeMemorySize()) + "/" + mib(system.getTotalMemorySize())
                        + ", process CPU load=" + system.getProcessCpuLoad();
            }
            emit("Startup still running: stage=" + stage + ", elapsed seconds="
                    + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - started) + ", " + memory);
            var threads = ManagementFactory.getThreadMXBean();
            stack(threads.getThreadInfo(serverThread.getId(), 32));
            // Do not allocate an unbounded full JVM stack dump on a memory-starved server.
            int remaining = 8;
            for (long id : threads.getAllThreadIds()) {
                if (closed || remaining == 0) break;
                if (id == serverThread.getId()) continue;
                ThreadInfo thread = threads.getThreadInfo(id);
                if (thread == null) continue;
                String name = thread.getThreadName().toLowerCase(Locale.ROOT);
                if (name.contains("c2me") || name.startsWith("worker-main-") || name.startsWith("meldrite compute worker")) {
                    stack(threads.getThreadInfo(thread.getThreadId(), 16));
                    remaining--;
                }
            }
        } catch (RuntimeException failure) {
            emit("STARTUP_DIAGNOSTICS_FAILED: " + failure);
        }
    }

    private void stack(ThreadInfo thread) {
        if (thread == null) return;
        var message = new StringBuilder("Startup thread: \"").append(thread.getThreadName())
                .append("\" state=").append(thread.getThreadState());
        if (thread.getLockName() != null) message.append(" waiting on ").append(thread.getLockName())
                .append(" owner=").append(thread.getLockOwnerName());
        for (var frame : thread.getStackTrace()) message.append("\n    at ").append(frame);
        emit(message.toString());
    }

    private void emit(String message) { if (!closed) output.accept(message); }
    private static long mib(long bytes) { return bytes < 0 ? -1 : bytes / (1024 * 1024); }

    @Override public void close() {
        closed = true;
        observer.shutdownNow();
    }
}
