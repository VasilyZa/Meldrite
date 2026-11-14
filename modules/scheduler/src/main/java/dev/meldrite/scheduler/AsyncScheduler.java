package dev.meldrite.scheduler;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
public final class AsyncScheduler implements AutoCloseable {
    public record Worker(TaskHandle task,Thread thread){}
    public record Shutdown(boolean terminated,int queued,List<Worker> running){public Shutdown{running=List.copyOf(running);}}
    public record Statistics(int threads,int poolSize,int active,int queued,int queueCapacity,long completed,long rejected){}
    private final int capacity;private final LongAdder rejected=new LongAdder();
    private final ThreadPoolExecutor executor;private final ConcurrentMap<Long,Worker> workers=new ConcurrentHashMap<>();private final AtomicLong sequence=new AtomicLong();private final Duration shutdownTimeout;
    private final ConcurrentMap<TaskHandle, SubmissionGroup> submissions = new ConcurrentHashMap<>();
    public AsyncScheduler(int threads,int capacity,Duration shutdownTimeout){if(threads<1||capacity<1||shutdownTimeout.isNegative())throw new IllegalArgumentException("Invalid async limits");this.capacity=capacity;this.shutdownTimeout=shutdownTimeout;var ids=new AtomicLong();executor=new ThreadPoolExecutor(threads,threads,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(capacity),runnable->{Thread thread=new Thread(runnable,"Meldrite async worker #"+ids.incrementAndGet());thread.setDaemon(true);thread.setContextClassLoader(AsyncScheduler.class.getClassLoader());return thread;},new ThreadPoolExecutor.AbortPolicy());executor.allowCoreThreadTimeOut(true);}
    public static AsyncScheduler configured(Duration timeout){var settings=WorkerSettings.system();return new AsyncScheduler(settings.asyncThreads(),settings.asyncQueueCapacity(),timeout);}
    private final class SubmissionGroup {
        private final TaskHandle handle;
        private final Runnable removal = this::cancel;
        // Accessed only inside submissions.compute operations.
        private int references;

        SubmissionGroup(TaskHandle handle) { this.handle = handle; }

        private void cancel() {
            executor.getQueue().removeIf(command -> ((Submission) command).group == this);
            submissions.remove(handle, this);
        }

        private void release() {
            SubmissionGroup remaining = submissions.computeIfPresent(handle, (task, current) -> {
                if (current != this) return current;
                return --references == 0 ? null : this;
            });
            if (remaining != this) handle.removeCancellationListener(removal);
        }
    }

    private final class Submission implements Runnable {
        private final SubmissionGroup group;
        private final Runnable operation;

        Submission(SubmissionGroup group, Runnable operation) { this.group = group; this.operation = operation; }

        @Override public void run() {
            long id=sequence.incrementAndGet();
            TaskHandle handle = group.handle;
            try {
                if(handle.cancelled()||!handle.owner().active()){handle.cancel();return;}
                workers.put(id,new Worker(handle,Thread.currentThread()));
                operation.run();
            } finally { workers.remove(id); group.release(); }
        }
    }
    void execute(TaskHandle handle, Runnable operation) {
        Objects.requireNonNull(handle);
        Objects.requireNonNull(operation);
        SubmissionGroup group = submissions.compute(handle, (task, current) -> {
            if (current == null) current = new SubmissionGroup(task);
            current.references++;
            return current;
        });
        try {
            handle.onCancel(group.removal);
            executor.execute(new Submission(group, operation));
        } catch (RejectedExecutionException failure) {
            group.release();
            rejected.increment();
            throw failure;
        } catch (RuntimeException | Error failure) {
            group.release();
            throw failure;
        }
        if (handle.cancelled()) group.cancel();
    }
    public Statistics statistics(){return new Statistics(executor.getMaximumPoolSize(),executor.getPoolSize(),executor.getActiveCount(),executor.getQueue().size(),capacity,executor.getCompletedTaskCount(),rejected.sum());}
    public List<Worker> workers(){return List.copyOf(workers.values());}
    public Shutdown shutdown(Duration timeout)throws InterruptedException{executor.shutdown();boolean terminated=executor.awaitTermination(timeout.toNanos(),TimeUnit.NANOSECONDS);return new Shutdown(terminated,executor.getQueue().size(),workers());}
    @Override public void close(){
        try{
            Shutdown result=shutdown(shutdownTimeout);
            if(result.terminated())return;
            // Bukkit/Paper cancels plugin work during server shutdown.  A
            // callback that ignores cancellation must not hold the whole
            // server lifecycle open indefinitely, so interrupt only after
            // the normal cooperative grace period has elapsed.
            cancelDiscarded();
            boolean terminated=executor.awaitTermination(Math.min(2,Math.max(1,shutdownTimeout.toSeconds())),TimeUnit.SECONDS);
            if(!terminated)throw new IllegalStateException("ASYNC_SHUTDOWN_TIMEOUT: "+workers.size()+" running, "+executor.getQueue().size()+" queued");
        }catch(InterruptedException failure){try{cancelDiscarded();}finally{Thread.currentThread().interrupt();}throw new IllegalStateException("Async shutdown interrupted",failure);}
    }
    private void cancelDiscarded(){for(Runnable discarded:executor.shutdownNow())((Submission)discarded).group.handle.cancel();}
}
