package dev.meldrite.scheduler;
import java.util.*;
import java.util.concurrent.atomic.*;
public final class TaskHandle implements AutoCloseable {
    public enum State { PENDING, RUNNING, COMPLETED, FAILED, CANCELLED }
    private static final AtomicInteger ids=new AtomicInteger();
    private final int id=ids.updateAndGet(value->{if(value==Integer.MAX_VALUE)throw new IllegalStateException("TASK_ID_EXHAUSTED");return value+1;});
    private final TaskOwner owner;private final boolean synchronous;private final Runnable remove;
    private final java.util.concurrent.CopyOnWriteArrayList<Runnable> cancellationListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    final AtomicReference<State> state=new AtomicReference<>(State.PENDING);
    TaskHandle(TaskOwner owner,boolean synchronous,Runnable remove){this.owner=Objects.requireNonNull(owner);this.synchronous=synchronous;this.remove=Objects.requireNonNull(remove);}
    public int id(){return id;}public TaskOwner owner(){return owner;}public boolean synchronous(){return synchronous;}public State state(){return state.get();}public boolean cancelled(){return state()==State.CANCELLED;}
    public void cancel(){State previous=state.getAndSet(State.CANCELLED);if(previous!=State.CANCELLED){remove.run();owner.detach(this);for(Runnable listener:cancellationListeners)try{listener.run();}finally{cancellationListeners.remove(listener);}}}
    void onCancel(Runnable listener){
        Objects.requireNonNull(listener);
        if(cancelled()){listener.run();return;}
        cancellationListeners.add(listener);
        if(cancelled()&&cancellationListeners.remove(listener))listener.run();
    }
    void removeCancellationListener(Runnable listener){cancellationListeners.remove(listener);}
    @Override public void close(){cancel();}
}
