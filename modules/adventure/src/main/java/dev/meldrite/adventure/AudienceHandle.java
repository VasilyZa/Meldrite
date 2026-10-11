package dev.meldrite.adventure;
import dev.meldrite.bridge.AudienceMessage.Target;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
public final class AudienceHandle implements AutoCloseable {
    public enum Kind { PLAYER, CONSOLE, BROADCAST }
    private final Target target;private final Locale locale;private final Runnable cleanup;
    private final AtomicBoolean closed=new AtomicBoolean();
    AudienceHandle(Target target,Locale locale,Runnable cleanup){this.target=Objects.requireNonNull(target);this.locale=Objects.requireNonNull(locale);this.cleanup=Objects.requireNonNull(cleanup);}
    public Target target(){return target;}public Locale locale(){return locale;}public boolean closed(){return closed.get();}
    @Override public void close(){if(closed.compareAndSet(false,true))cleanup.run();}
}
