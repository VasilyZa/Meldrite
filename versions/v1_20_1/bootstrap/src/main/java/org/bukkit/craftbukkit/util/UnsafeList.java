package org.bukkit.craftbukkit.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** Array-backed list with the unchecked accessor used by server hot paths. */
public class UnsafeList<E> extends AbstractList<E> implements RandomAccess, Cloneable, Serializable {
    private static final long serialVersionUID = 8683452581112892191L;
    private Object[] data;
    private int size;
    private final int initialCapacity;
    private int maxSize;

    public UnsafeList() { this(32); }
    public UnsafeList(int capacity) { this(capacity, 5); }
    public UnsafeList(int capacity, int ignoredIteratorPool) {
        int normalized = Math.max(1, capacity);
        int rounded = Integer.highestOneBit(normalized - 1) << 1;
        if (rounded < normalized) rounded = normalized;
        data = new Object[rounded];
        initialCapacity = rounded;
    }
    @SuppressWarnings("unchecked") @Override public E get(int index) { rangeCheck(index); return (E) data[index]; }
    @SuppressWarnings("unchecked") public E unsafeGet(int index) { return (E) data[index]; }
    @SuppressWarnings("unchecked") @Override public E set(int index, E value) { rangeCheck(index); E old = (E) data[index]; data[index] = value; return old; }
    @Override public boolean add(E value) { grow(); data[size++] = value; modCount++; return true; }
    @Override public void add(int index, E value) { if (index < 0 || index > size) throw new IndexOutOfBoundsException(); grow(); System.arraycopy(data, index, data, index + 1, size - index); data[index] = value; size++; modCount++; }
    @SuppressWarnings("unchecked") @Override public E remove(int index) { rangeCheck(index); E old = (E) data[index]; int moved = size - index - 1; if (moved > 0) System.arraycopy(data, index + 1, data, index, moved); data[--size] = null; modCount++; return old; }
    @Override public int size() { return size; }
    @Override public void clear() { Arrays.fill(data, 0, size, null); size = 0; maxSize = 0; modCount++; }
    public void setSize(int newSize) { if (newSize < 0 || newSize > data.length) throw new IndexOutOfBoundsException(); maxSize = Math.max(maxSize, size); size = newSize; modCount++; }
    public void completeReset() { Arrays.fill(data, 0, Math.max(size, maxSize), null); size = maxSize = 0; modCount++; }
    public void trimToSize() { if (size < data.length) data = Arrays.copyOf(data, Math.max(1, size)); }
    @Override public Object clone() { try { UnsafeList<?> copy = (UnsafeList<?>) super.clone(); copy.data = Arrays.copyOf(data, size); return copy; } catch (CloneNotSupportedException impossible) { throw new AssertionError(impossible); } }
    private void grow() { if (size == data.length) data = Arrays.copyOf(data, Math.max(1, data.length << 1)); }
    private void rangeCheck(int index) { if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size); }

    public final class Itr implements Iterator<E> {
        private int cursor;
        private int last = -1;
        @Override public boolean hasNext() { return cursor < size; }
        @Override public E next() { if (!hasNext()) throw new NoSuchElementException(); last = cursor++; return unsafeGet(last); }
        @Override public void remove() { if (last < 0) throw new IllegalStateException(); UnsafeList.this.remove(last); cursor = last; last = -1; }
    }
    @Override public Iterator<E> iterator() { return new Itr(); }
}
