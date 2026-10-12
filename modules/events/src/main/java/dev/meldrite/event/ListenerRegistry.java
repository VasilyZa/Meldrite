package dev.meldrite.event;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
import java.util.function.*;
public final class ListenerRegistry<E> {
    public enum Priority { LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR }
    @FunctionalInterface public interface Call<E>{void accept(E event)throws Throwable;}
    public record Registration<E>(String owner,Priority priority,long order,boolean ignoreCancelled,BooleanSupplier enabled,Call<E> call){public Registration{Objects.requireNonNull(owner);Objects.requireNonNull(priority);Objects.requireNonNull(enabled);Objects.requireNonNull(call);}}
    public record Failure(String owner,Throwable cause){public Failure{Objects.requireNonNull(owner);Objects.requireNonNull(cause);}}
    /** Access to an ABI-owned listener without allocating a neutral wrapper for each dispatch. */
    public interface OrderedListenerAccess<E,L> {
        String owner(L listener);
        boolean enabled(L listener);
        boolean ignoreCancelled(L listener);
        boolean cancelled(E event);
        void call(L listener,E event)throws Throwable;
    }
    private final List<Registration<E>> registrations=new ArrayList<>();private long sequence;
    public synchronized AutoCloseable register(String owner,PluginScope scope,Priority priority,boolean ignoreCancelled,BooleanSupplier enabled,Call<E> call){Registration<E> registration=new Registration<>(owner,priority,++sequence,ignoreCancelled,enabled,call);registrations.add(registration);return scope.register(()->unregister(registration));}
    private synchronized void unregister(Registration<E> registration){registrations.remove(registration);}
    public synchronized List<Registration<E>> snapshot(){return List.copyOf(registrations);}
    public void dispatch(E event,BooleanSupplier cancelled,Consumer<Failure> failures){dispatchSnapshot(event,cancelled,snapshot(),failures);}
    /** Sort neutral registrations whose source does not already provide a baked order. */
    public void dispatchSnapshot(E event,BooleanSupplier cancelled,List<Registration<E>> snapshot,Consumer<Failure> failures){
        var ordered=snapshot.stream().sorted(Comparator.comparing((Registration<E> r)->r.priority().ordinal()).thenComparingLong(Registration::order)).toList();
        for(Registration<E> registration:ordered){if(!registration.enabled().getAsBoolean()||(registration.ignoreCancelled()&&cancelled.getAsBoolean()))continue;
            try{registration.call().accept(event);}catch(Throwable failure){reportFailure(registration.owner(),failure,failures);}
        }
    }
    /** Borrow the authoritative priority/registration-ordered array; never copy, mutate or retain it. */
    public static <E,L> void dispatchOrdered(E event,L[] snapshot,OrderedListenerAccess<E,L> access,Consumer<Failure> failures){
        for(L listener:snapshot){
            if(!access.enabled(listener)||(access.ignoreCancelled(listener)&&access.cancelled(event)))continue;
            try{access.call(listener,event);}catch(Throwable failure){reportFailure(access.owner(listener),failure,failures);}
        }
    }
    private static void reportFailure(String owner,Throwable failure,Consumer<Failure> failures){
        if(failure instanceof InterruptedException)Thread.currentThread().interrupt();
        failures.accept(new Failure(owner,failure));
    }
}
