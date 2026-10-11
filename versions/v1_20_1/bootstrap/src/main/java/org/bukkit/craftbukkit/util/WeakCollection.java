package org.bukkit.craftbukkit.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Collection that automatically drops values no longer strongly reachable. */
public final class WeakCollection<T> implements Collection<T> {
    private final Collection<WeakReference<T>> values = new ArrayList<>();

    @Override public boolean add(T value) { return values.add(new WeakReference<>(Objects.requireNonNull(value, "value"))); }
    @Override public boolean addAll(Collection<? extends T> source) {
        boolean changed = false;
        for (T value : source) changed |= add(value);
        return changed;
    }
    @Override public void clear() { values.clear(); }
    @Override public boolean contains(Object value) {
        if (value == null) return false;
        for (T current : this) if (value.equals(current)) return true;
        return false;
    }
    @Override public boolean containsAll(Collection<?> source) { for (Object value : source) if (!contains(value)) return false; return true; }
    @Override public boolean isEmpty() { return !iterator().hasNext(); }
    @Override public Iterator<T> iterator() {
        Iterator<WeakReference<T>> iterator = values.iterator();
        return new Iterator<>() {
            private T next;
            private boolean ready;
            private boolean removable;
            @Override public boolean hasNext() {
                if (ready) return true;
                while (iterator.hasNext()) {
                    T current = iterator.next().get();
                    if (current == null) { iterator.remove(); continue; }
                    next = current; ready = true; removable = true; return true;
                }
                return false;
            }
            @Override public T next() { if (!hasNext()) throw new NoSuchElementException(); T result = next; next = null; ready = false; return result; }
            @Override public void remove() { if (!removable) throw new IllegalStateException(); iterator.remove(); removable = false; }
        };
    }
    @Override public boolean remove(Object value) { Iterator<T> it = iterator(); while (it.hasNext()) if (Objects.equals(value, it.next())) { it.remove(); return true; } return false; }
    @Override public boolean removeAll(Collection<?> source) { boolean changed = false; Iterator<T> it = iterator(); while (it.hasNext()) if (source.contains(it.next())) { it.remove(); changed = true; } return changed; }
    @Override public boolean retainAll(Collection<?> source) { boolean changed = false; Iterator<T> it = iterator(); while (it.hasNext()) if (!source.contains(it.next())) { it.remove(); changed = true; } return changed; }
    @Override public int size() { int count = 0; for (T ignored : this) count++; return count; }
    @Override public Object[] toArray() { ArrayList<T> result = new ArrayList<>(); for (T value : this) result.add(value); return result.toArray(); }
    @Override public <E> E[] toArray(E[] array) { ArrayList<T> result = new ArrayList<>(); for (T value : this) result.add(value); return result.toArray(array); }
}
