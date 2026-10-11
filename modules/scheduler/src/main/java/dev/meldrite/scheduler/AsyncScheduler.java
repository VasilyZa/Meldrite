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
    public AsyncScheduler(int threads,int capacity,Duration shutdownTimeout){if(threads<1||capacity<1||shutdownTimeout.isNegative())throw new IllegalArgumentException("Invalid async limits");this.capacity=capacity;this.shutdownTimeout=shutdownTimeout;var ids=new AtomicLong();executor=new ThreadPoolExecutor(threads,threads,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(capacity),runnable->{Thread thread=new Thread(runnable,"Meldrite async worker #"+ids.incrementAndGet());thread.setDaemon(true);thread.setContextClassLoader(AsyncScheduler.class.getClassLoader());return thread;},new ThreadPoolExecutor.AbortPolicy());executor.allowCoreThreadTimeOut(true);}
    public static AsyncScheduler configured(Duration timeout){var settings=WorkerSettings.system();return new AsyncScheduler(settings.asyncThreads(),settings.asyncQueueCapacity(),timeout);}
    void execute(TaskHandle handle,Runnable operation){try{executor.execute(()->{
        long id=sequence.incrementAndGet();
        // A task can be cancelled after submission but before a worker dequeues
        // it.  ThreadPoolExecutor still runs queued Runnables during shutdown,
        // so the cancellation check must guard the callback itself.
        if(handle.cancelled()||!handle.owner().active())return;
        workers.put(id,new Worker(handle,Thread.currentThread()));
        try{operation.run();}finally{workers.remove(id);}
    });}catch(RejectedExecutionException failure){rejected.increment();throw failure;}}
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
            executor.shutdownNow();
            boolean terminated=executor.awaitTermination(Math.min(2,Math.max(1,shutdownTimeout.toSeconds())),TimeUnit.SECONDS);
            if(!terminated)throw new IllegalStateException("ASYNC_SHUTDOWN_TIMEOUT: "+workers.size()+" running, "+executor.getQueue().size()+" queued");
        }catch(InterruptedException failure){Thread.currentThread().interrupt();throw new IllegalStateException("Async shutdown interrupted",failure);}
    }
}
