package dev.meldrite.v1201.perf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Executor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;

/** Main-thread completion admission; the queued Runnable owns future completion. */
public final class ChunkCompletionBudget {
    public static final int MAX_PENDING = 4096;
    public static final int MAX_PER_TICK = 64;
    public static final long NANOS_PER_TICK = 2_000_000L;
    private static final ThreadLocal<ArrayList<ChunkCompletionBudget>> LOCAL =
            ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Boolean> NEXT_IDLE_WAIT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> WAIT_DEPTH = ThreadLocal.withInitial(() -> 0);

    private static final class Pending {
        final Runnable command;
        final long distance;
        final long order;
        boolean completed;
        Pending(Runnable command, long distance, long order) {
            this.command = command;
            this.distance = distance;
            this.order = order;
        }
    }

    private final ServerWorld world;
    private final ThreadExecutor<Runnable> main;
    private final ArrayDeque<Pending> fifo = new ArrayDeque<>();
    private final PriorityQueue<Pending> nearby = new PriorityQueue<>(
            Comparator.comparingLong((Pending p) -> p.distance).thenComparingLong(p -> p.order));
    private boolean registered;
    private boolean started;
    private boolean closed;
    private boolean draining;
    private int pending;
    private int lastTick = Integer.MIN_VALUE;
    private long sequence;

    public ChunkCompletionBudget(ServerWorld world, ThreadExecutor<Runnable> main) {
        this.world = world;
        this.main = main;
    }

    public Executor executor(ChunkPos position) {
        // Called by CompletableFuture on arbitrary producer threads. All world/player
        // reads and every queue mutation happen in the executor's main-thread callback.
        return command -> main.execute(() -> enqueue(command, position));
    }

    private void checkThread() {
        if (!main.isOnThread()) throw new IllegalStateException("Chunk completion must run on the main thread");
    }

    private void register() {
        if (!registered) {
            LOCAL.get().add(this);
            registered = true;
        }
    }

    private void enqueue(Runnable command, ChunkPos position) {
        checkThread();
        if (!started || closed || WAIT_DEPTH.get() != 0 || pending >= MAX_PENDING) {
            command.run();
            return;
        }
        register();
        long distance = Long.MAX_VALUE;
        for (var player : world.getPlayers()) {
            ChunkPos playerPos = player.getChunkPos();
            long dx = Math.abs((long) playerPos.x - position.x);
            long dz = Math.abs((long) playerPos.z - position.z);
            distance = Math.min(distance, Math.max(dx, dz));
        }
        Pending task = new Pending(command, distance, sequence++);
        fifo.addLast(task);
        nearby.add(task);
        pending++;
    }

    /** Called once per storage tick; server tick number prevents duplicate allowances. */
    public void tick() {
        checkThread();
        if (closed) return;
        int tick = world.getServer().getTicks();
        // Initial world preparation pumps executors before ordinary server ticks exist.
        if (tick <= 0) return;
        started = true;
        if (tick == lastTick) return;
        lastTick = tick;
        drain(false);
    }

    private Pending next(boolean oldest) {
        Pending task;
        if (oldest) {
            do { task = fifo.pollFirst(); } while (task != null && task.completed);
        } else {
            do { task = nearby.poll(); } while (task != null && task.completed);
        }
        return task;
    }

    private void drain(boolean all) {
        checkThread();
        if (draining && !all) return;
        boolean previousDraining = draining;
        draining = true;
        long start = System.nanoTime();
        int count = 0;
        try {
            while (pending != 0 && (all || count < MAX_PER_TICK
                    && (count == 0 || System.nanoTime() - start < NANOS_PER_TICK))) {
                // Reserve one in four admissions for FIFO, preventing far-chunk starvation.
                Pending task = next(all || (count & 3) == 0);
                if (task == null) throw new IllegalStateException("Missing queued chunk completion");
                task.completed = true;
                pending--;
                count++;
                task.command.run();
            }
        } finally {
            draining = previousDraining;
            // Bound duplicate-index tombstones even under a continuous stream of work.
            fifo.removeIf(task -> task.completed);
            nearby.removeIf(task -> task.completed);
            if (pending == 0 && registered) {
                LOCAL.get().remove(this);
                registered = false;
                if (LOCAL.get().isEmpty()) LOCAL.remove();
            }
        }
    }

    public void close() {
        checkThread();
        closed = true;
        drain(true);
    }

    public static void runAllPending() {
        // A completion may register another world while executing. Re-snapshot each pass.
        while (!LOCAL.get().isEmpty()) {
            for (ChunkCompletionBudget budget : new ArrayList<>(LOCAL.get())) {
                budget.drain(true);
            }
        }
        if (LOCAL.get().isEmpty()) LOCAL.remove();
    }

    /** Exclude only the server's ordinary tick-end idle pump, not nested future waits. */
    public static void runTickIdleWait(Runnable wait) {
        Boolean previous = NEXT_IDLE_WAIT.get();
        NEXT_IDLE_WAIT.set(true);
        try { wait.run(); }
        finally {
            if (previous == null) NEXT_IDLE_WAIT.remove();
            else NEXT_IDLE_WAIT.set(previous);
        }
    }

    public static boolean consumeTickIdleWait() {
        boolean idle = Boolean.TRUE.equals(NEXT_IDLE_WAIT.get());
        NEXT_IDLE_WAIT.remove();
        return idle;
    }

    /** Pair in a try/finally around ThreadExecutor.runTasks(BooleanSupplier). */
    public static void enterSynchronousWait() {
        WAIT_DEPTH.set(WAIT_DEPTH.get() + 1);
        try { runAllPending(); }
        catch (RuntimeException | Error failure) {
            exitSynchronousWait();
            throw failure;
        }
    }

    public static void exitSynchronousWait() {
        int depth = WAIT_DEPTH.get() - 1;
        if (depth == 0) WAIT_DEPTH.remove();
        else WAIT_DEPTH.set(depth);
    }
}
