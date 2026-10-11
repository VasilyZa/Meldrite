package dev.meldrite.event;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
import java.util.function.*;
public final class ListenerRegistry<E> {
    public enum Priority { LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR }
    @FunctionalInterface public interface Call<E>{void accept(E event)throws Throwable;}
    public record Registration<E>(String owner,Priority priority,long order,boolean ignoreCancelled,BooleanSupplier enabled,Call<E> call){public Registration{Objects.requireNonNull(owner);Objects.requireNonNull(priority);Objects.requireNonNull(enabled);Objects.requireNonNull(call);}}
    public record Failure(String owner,Throwable cause){public Failure{Objects.requireNonNull(owner);Objects.requireNonNull(cause);}}
    private final List<Registration<E>> registrations=new ArrayList<>();private long sequence;
    public synchronized AutoCloseable register(String owner,PluginScope scope,Priority priority,boolean ignoreCancelled,BooleanSupplier enabled,Call<E> call){Registration<E> registration=new Registration<>(owner,priority,++sequence,ignoreCancelled,enabled,call);registrations.add(registration);return scope.register(()->unregister(registration));}
    private synchronized void unregister(Registration<E> registration){registrations.remove(registration);}
    public synchronized List<Registration<E>> snapshot(){return List.copyOf(registrations);}
    public void dispatch(E event,BooleanSupplier cancelled,Consumer<Failure> failures){dispatchSnapshot(event,cancelled,snapshot(),failures);}
    /** HandlerList adapters provide a snapshot here, using the same common sorting/error algorithm. */
    public void dispatchSnapshot(E event,BooleanSupplier cancelled,List<Registration<E>> snapshot,Consumer<Failure> failures){
        var ordered=snapshot.stream().sorted(Comparator.comparing((Registration<E> r)->r.priority().ordinal()).thenComparingLong(Registration::order)).toList();
        for(Registration<E> registration:ordered){if(!registration.enabled().getAsBoolean()||(registration.ignoreCancelled()&&cancelled.getAsBoolean()))continue;
            try{registration.call().accept(event);}catch(Throwable failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();failures.accept(new Failure(registration.owner(),failure));}
        }
    }
}
