package dev.meldrite.craft;
import java.lang.ref.*;
import java.util.*;
import java.util.function.*;
/** Cache owns neutral identity keys and weak facade references, never a native world/entity. */
public final class WrapperCache {
    private static final class Entry extends WeakReference<Object> {final Object key;Entry(Object key,Object value,ReferenceQueue<Object> queue){super(value,queue);this.key=key;}}
    private final Map<Object,Entry> entries=new HashMap<>();private final ReferenceQueue<Object> queue=new ReferenceQueue<>();
    private void drain(){Entry entry;while((entry=(Entry)queue.poll())!=null)entries.remove(entry.key,entry);}
    @SuppressWarnings("unchecked") public synchronized <T>T get(Object key,Supplier<T> factory){Objects.requireNonNull(key);drain();Entry existing=entries.get(key);Object value=existing==null?null:existing.get();if(value==null){value=Objects.requireNonNull(factory.get(),"Facade creator returned null");entries.put(key,new Entry(key,value,queue));}return (T)value;}
    public synchronized void evict(Predicate<Object> predicate){drain();entries.keySet().removeIf(predicate);}
    public synchronized void rebind(Object previous,Object replacement,Object facade){
        Objects.requireNonNull(previous);Objects.requireNonNull(replacement);Objects.requireNonNull(facade);drain();
        Entry old=entries.get(previous),next=entries.get(replacement);
        if(old==null||old.get()!=facade)throw new IllegalStateException("FACADE_REBIND_NOT_PUBLISHED");
        if(next!=null&&next.get()!=null&&next.get()!=facade)throw new IllegalStateException("FACADE_REBIND_ALREADY_PUBLISHED");
        entries.remove(previous);entries.put(replacement,new Entry(replacement,facade,queue));
    }
    public synchronized int entries(){drain();return entries.size();}
}
