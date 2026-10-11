package dev.meldrite.scheduler;

import dev.meldrite.bridge.ExecutionPort;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/** Tick clock only: no wall-clock catchup or version conditionals. Callbacks execute outside the queue lock. */
public final class TickScheduler implements AutoCloseable {
    public record Failure(int taskId,String owner,Throwable cause){}
    private final class Entry {
        final TaskHandle handle;final long order,period;long due;
        Optional<Consumer<TaskHandle>> action;Optional<GuardedFuture<?>> future;final Optional<AsyncScheduler> async;int inFlight;boolean queued;
        Entry(TaskOwner owner,long delay,long period,Consumer<TaskHandle> action,Optional<GuardedFuture<?>> future,Optional<AsyncScheduler> async){
            this.period=period;this.order=++sequence;this.due=Math.addExact(tick,delay);this.action=Optional.of(action);this.future=future;this.async=async;
            this.handle=new TaskHandle(owner,async.isEmpty(),()->remove(this));
        }
    }
    private final class GuardedFuture<T> extends CompletableFuture<T> {
        private volatile Optional<TaskHandle> handle=Optional.empty();
        void bind(TaskHandle task){handle=Optional.of(task);if(isCancelled())task.cancel();}
        @Override public boolean cancel(boolean mayInterrupt){boolean result=super.cancel(false);if(result)handle.ifPresent(TaskHandle::cancel);return result;}
        private void guard(){if(!isDone()&&execution.isPrimaryThread())throw new IllegalStateException("PRIMARY_THREAD_WAIT_REJECTED");}
        @Override public T get()throws InterruptedException,ExecutionException{guard();return super.get();}
        @Override public T get(long timeout,TimeUnit unit)throws InterruptedException,ExecutionException,TimeoutException{guard();return super.get(timeout,unit);}
        @Override public T join(){guard();return super.join();}
    }
    private final ExecutionPort execution;private final Consumer<Failure> failures;
    private final PriorityQueue<Entry> queue=new PriorityQueue<>(Comparator.comparingLong((Entry entry)->entry.due).thenComparingLong(entry->entry.order));
    private final Map<Integer,Entry> tasks=new LinkedHashMap<>();private final Map<Integer,Integer> running=new LinkedHashMap<>();
    private long tick,sequence,lastDrain=Long.MIN_VALUE;private boolean closed,draining;
    public TickScheduler(ExecutionPort execution,long initialTick,Consumer<Failure> failures){this.execution=Objects.requireNonNull(execution);this.tick=initialTick;this.failures=Objects.requireNonNull(failures);}
    public TaskHandle schedule(TaskOwner owner,long delay,long period,Runnable task){Objects.requireNonNull(task);return schedule(owner,delay,period,handle->task.run());}
    public TaskHandle schedule(TaskOwner owner,long delay,long period,Consumer<TaskHandle> task){return enqueue(owner,delay,period,Objects.requireNonNull(task),Optional.empty(),Optional.empty());}
    public TaskHandle scheduleAsync(TaskOwner owner,long delay,long period,Consumer<TaskHandle> task,AsyncScheduler async){return enqueue(owner,delay,period,Objects.requireNonNull(task),Optional.empty(),Optional.of(Objects.requireNonNull(async)));}
    private synchronized TaskHandle enqueue(TaskOwner owner,long delay,long period,Consumer<TaskHandle> task,Optional<GuardedFuture<?>> future,Optional<AsyncScheduler> async){
        Objects.requireNonNull(owner);owner.check();if(closed)throw new IllegalStateException("SCHEDULER_CLOSED");
        if(delay<0||(period!=-1&&period<1))throw new IllegalArgumentException("Invalid normalized tick schedule");
        Entry entry=new Entry(owner,delay,period,task,future,async);owner.attach(entry.handle);tasks.put(entry.handle.id(),entry);queue.add(entry);entry.queued=true;return entry.handle;
    }
    public <T> Future<T> callSync(TaskOwner owner,Callable<T> task){
        Objects.requireNonNull(task);var future=new GuardedFuture<T>();
        TaskHandle handle=enqueue(owner,0,-1,taskHandle->{try{future.complete(task.call());}catch(Throwable failure){future.completeExceptionally(failure);throw new CompletionException(failure);}},Optional.of(future),Optional.empty());future.bind(handle);return future;
    }
    private synchronized void remove(Entry entry){
        tasks.remove(entry.handle.id());queue.remove(entry);entry.queued=false;entry.action=Optional.empty();entry.future.ifPresent(future->future.cancel(false));entry.future=Optional.empty();
    }
    public void tick(long nextTick){
        if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_TICK_DRAIN");
        synchronized(this){if(closed)throw new IllegalStateException("SCHEDULER_CLOSED");if(draining)throw new IllegalStateException("REENTRANT_TICK_DRAIN");if(nextTick<=lastDrain||nextTick<tick)throw new IllegalArgumentException("Non-monotonic heartbeat");tick=nextTick;lastDrain=nextTick;draining=true;}
        try{while(true){Entry entry;Consumer<TaskHandle> action;
            synchronized(this){entry=queue.peek();if(entry==null||entry.due>nextTick)break;queue.remove();entry.queued=false;
                if(entry.handle.cancelled()||!entry.handle.owner().active()){entry.handle.cancel();continue;}
                action=entry.action.orElseThrow();entry.inFlight++;
                if(entry.period>0){try{entry.due=Math.addExact(nextTick,entry.period);queue.add(entry);entry.queued=true;}catch(ArithmeticException failure){finish(entry,false,failure);failures.accept(new Failure(entry.handle.id(),entry.handle.owner().identity(),failure));continue;}}
            }
            if(entry.async.isPresent())try{entry.async.get().execute(entry.handle,()->run(entry,action));}catch(RejectedExecutionException failure){finish(entry,false,failure);failures.accept(new Failure(entry.handle.id(),entry.handle.owner().identity(),failure));}
            else run(entry,action);
        }}finally{synchronized(this){draining=false;}}
    }
    private void run(Entry entry,Consumer<TaskHandle> action){
        synchronized(this){if(entry.handle.cancelled()||!entry.handle.owner().active()){finish(entry,false,null);return;}running.merge(entry.handle.id(),1,Integer::sum);entry.handle.state.set(TaskHandle.State.RUNNING);}
        Throwable failure=null;
        try{action.accept(entry.handle);}catch(Throwable caught){failure=caught instanceof CompletionException completion&&completion.getCause()!=null?completion.getCause():caught;if(failure instanceof InterruptedException)Thread.currentThread().interrupt();}
        finally{finish(entry,true,failure);}
        if(failure!=null)failures.accept(new Failure(entry.handle.id(),entry.handle.owner().identity(),failure));
    }
    private synchronized void finish(Entry entry,boolean started,Throwable failure){
        if(started){int count=running.getOrDefault(entry.handle.id(),0);if(count<=1)running.remove(entry.handle.id());else running.put(entry.handle.id(),count-1);}
        entry.inFlight--;
        if(entry.handle.cancelled()||!entry.handle.owner().active()){entry.handle.cancel();}
        else if(!entry.queued&&entry.inFlight==0){entry.handle.state.set(failure==null?TaskHandle.State.COMPLETED:TaskHandle.State.FAILED);remove(entry);entry.handle.owner().detach(entry.handle);}
        else entry.handle.state.set(running.containsKey(entry.handle.id())?TaskHandle.State.RUNNING:TaskHandle.State.PENDING);
    }
    public synchronized int pending(){return queue.size();}
    public synchronized boolean queued(int id){Entry entry=tasks.get(id);return entry!=null&&queue.contains(entry)&&!entry.handle.cancelled();}
    public synchronized boolean running(int id){return running.containsKey(id);}
    public synchronized List<TaskHandle> tasks(){return tasks.values().stream().map(entry->entry.handle).toList();}
    @Override public void close(){
        if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_SCHEDULER_CLOSE");List<TaskHandle> remaining;
        synchronized(this){if(closed)return;closed=true;remaining=tasks.values().stream().map(entry->entry.handle).toList();}
        for(TaskHandle task:remaining)task.cancel();
    }
}
