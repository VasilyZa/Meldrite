package dev.meldrite.event;
import dev.meldrite.bridge.*;
import java.util.*;
import java.util.function.*;
public final class EventCoordinator {
    public static final class UnrepresentableInput extends RuntimeException {public UnrepresentableInput(String reason){super(reason);}}
    @FunctionalInterface public interface Factory<E>{E create(ActionSnapshot snapshot)throws Exception;}
    private final EventPolicy policy;private final Consumer<Throwable> failures;private final ThreadLocal<Integer> depth=ThreadLocal.withInitial(()->0);
    public EventCoordinator(EventPolicy policy,Consumer<Throwable> failures){this.policy=Objects.requireNonNull(policy);this.failures=Objects.requireNonNull(failures);}
    public <E> ActionDecision dispatch(ActionToken token,ActionSnapshot snapshot,boolean asynchronous,Factory<E> factory,Consumer<E> dispatch,Function<E,ActionDecision> decide){
        token.validate(snapshot);policy.check(asynchronous);
        if(token.decision!=null)return token.decision;
        if(token.dispatching){token.forcedAbort="REENTRANT_SAME_ACTION";return ActionDecision.abort(token.forcedAbort);}
        int previous=depth.get();token.input=snapshot;
        if(previous>=policy.maximumDepth()){token.decision=ActionDecision.abort("EVENT_RECURSION_LIMIT");return token.decision;}
        token.dispatching=true;depth.set(previous+1);
        try{
            E event=Objects.requireNonNull(factory.create(snapshot),"Event factory returned null");
            dispatch.accept(event);
            token.decision=token.forcedAbort==null?Objects.requireNonNull(decide.apply(event),"Event decision returned null"):ActionDecision.abort(token.forcedAbort);
        }catch(UnrepresentableInput failure){token.decision=ActionDecision.abort("UNREPRESENTABLE_EVENT_INPUT: "+failure.getMessage());failures.accept(failure);}
        catch(Throwable failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();token.decision=ActionDecision.abort("INTERNAL_ACTION_FAILURE");failures.accept(failure);}
        finally{token.dispatching=false;if(previous==0)depth.remove();else depth.set(previous);}
        return token.decision;
    }
}
