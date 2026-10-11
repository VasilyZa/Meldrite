package org.bukkit.craftbukkit.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/** Set facade which delays allocation until its first operation. */
public abstract class LazyHashSet<E> implements Set<E> {
    private Set<E> reference;

    protected abstract Set<E> makeReference();

    public final Set<E> getReference() {
        Set<E> current = reference;
        return current != null ? current : (reference = makeReference());
    }

    public final boolean isLazy() { return reference == null; }
    @Override public int size() { return getReference().size(); }
    @Override public boolean isEmpty() { return getReference().isEmpty(); }
    @Override public boolean contains(Object value) { return getReference().contains(value); }
    @Override public Iterator<E> iterator() { return getReference().iterator(); }
    @Override public Object[] toArray() { return getReference().toArray(); }
    @Override public <T> T[] toArray(T[] array) { return getReference().toArray(array); }
    @Override public boolean add(E value) { return getReference().add(value); }
    @Override public boolean remove(Object value) { return getReference().remove(value); }
    @Override public boolean containsAll(Collection<?> values) { return getReference().containsAll(values); }
    @Override public boolean addAll(Collection<? extends E> values) { return getReference().addAll(values); }
    @Override public boolean retainAll(Collection<?> values) { return getReference().retainAll(values); }
    @Override public boolean removeAll(Collection<?> values) { return getReference().removeAll(values); }
    @Override public void clear() { getReference().clear(); }
    @Override public int hashCode() { return getReference().hashCode(); }
    @Override public boolean equals(Object value) { return value == this || getReference().equals(value); }
    @Override public String toString() { return getReference().toString(); }
}
