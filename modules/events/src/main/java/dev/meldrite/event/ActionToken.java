package dev.meldrite.event;
import dev.meldrite.bridge.*;
import java.util.*;
/** Owned by one native transaction; hooks share this object and close it at transaction end. */
public final class ActionToken implements AutoCloseable {
    private final String identity;private final Thread thread=Thread.currentThread();
    ActionSnapshot input;ActionDecision decision;String forcedAbort;boolean dispatching,closed;
    public ActionToken(){this(UUID.randomUUID().toString());}
    public ActionToken(String identity){this.identity=Objects.requireNonNull(identity,"identity");if(identity.isBlank())throw new IllegalArgumentException("identity");}
    public String identity(){return identity;}
    void validate(ActionSnapshot snapshot){if(Thread.currentThread()!=thread)throw new IllegalStateException("FOREIGN_ACTION_THREAD");if(closed)throw new IllegalStateException("ACTION_TOKEN_CLOSED");if(!identity.equals(snapshot.token()))throw new IllegalArgumentException("ACTION_TOKEN_MISMATCH");if(input!=null&&!input.equals(snapshot))throw new IllegalArgumentException("ACTION_INPUT_CHANGED");}
    @Override public void close(){if(Thread.currentThread()!=thread)throw new IllegalStateException("FOREIGN_ACTION_THREAD");if(dispatching)throw new IllegalStateException("ACTION_STILL_DISPATCHING");closed=true;input=null;decision=null;}
}
