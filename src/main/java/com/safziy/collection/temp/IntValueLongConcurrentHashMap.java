package com.safziy.collection.temp;

import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Segments are specialized versions of hash tables.  This
 * subclasses from ReentrantLock opportunistically, just to
 * simplify some locking and avoid separate construction.
 */
public class IntValueLongConcurrentHashMap{
    /*
     * Segments maintain a table of entry lists that are ALWAYS
     * kept in a consistent state, so can be read without locking.
     * Next fields of nodes are immutable (final).  All list
     * additions are performed at the front of each bin. This
     * makes it easy to check changes, and also fast to traverse.
     * When nodes would otherwise be changed, new nodes are
     * created to replace them. This works well for hash tables
     * since the bin lists tend to be short. (The average length
     * is less than two for the default load factor threshold.)
     *
     * Read operations can thus proceed without locking, but rely
     * on selected uses of volatiles to ensure that completed
     * write operations performed by other threads are
     * noticed. For most purposes, the "count" field, tracking the
     * number of elements, serves as that volatile variable
     * ensuring visibility.  This is convenient because this field
     * needs to be read in many read operations anyway:
     *
     *   - All (unsynchronized) read operations must first read the
     *     "count" field, and should not look at table entries if
     *     it is 0.
     *
     *   - All (synchronized) write operations should write to
     *     the "count" field after structurally changing any bin.
     *     The operations must not take any action that could even
     *     momentarily cause a concurrent read operation to see
     *     inconsistent data. This is made easier by the nature of
     *     the read operations in Map. For example, no operation
     *     can reveal that the table has grown but the threshold
     *     has not yet been updated, so there are no atomicity
     *     requirements for this with respect to reads.
     *
     * As a guide, all critical volatile reads and writes to the
     * count field are marked in code comments.
     */

    private static final long serialVersionUID = 2249069246763182397L;

    /**
     * The maximum capacity, used if a higher value is implicitly
     * specified by either of the constructors with arguments.  MUST
     * be a power of two <= 1<<30 to ensure that entries are indexable
     * using ints.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;
    /**
     * The number of elements in this segment's region.
     */
    transient volatile int count;

    /**
     * The table is rehashed when its size exceeds this threshold.
     * (The value of this field is always <tt>(int)(capacity *
     * loadFactor)</tt>.)
     */
    transient int threshold;

    /**
     * The per-segment table.
     */
    transient volatile HashEntry[] table;

    /**
     * The load factor for the hash table.  Even though this value
     * is same for all segments, it is replicated to avoid needing
     * links to outer object.
     * @serial
     */
    final float loadFactor;

    public IntValueLongConcurrentHashMap(int initialCapacity, float lf){
        loadFactor = lf;

        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;

        setTable(HashEntry.newArray(capacity));
    }

    public IntValueLongConcurrentHashMap(int initialCapacity){
        this(initialCapacity, 0.75f);
    }

    public IntValueLongConcurrentHashMap(){
        this(16, 0.75f);
    }

    /**
     * Sets table to new HashEntry array.
     * Call only while holding lock or in constructor.
     */
    void setTable(HashEntry[] newTable){
        threshold = (int) (newTable.length * loadFactor);
        table = newTable;
    }

    /**
     * Returns properly casted first entry of bin for given hash.
     */
    HashEntry getFirst(long hash){
        HashEntry[] tab = table;
        return tab[((int) hash) & (tab.length - 1)];
    }

    /* Specialized implementations of map methods */

    public int size(){
        return count;
    }

    public int get(long key){
        if (count != 0){ // read-volatile
            HashEntry e = getFirst(key);
            while (e != null){
                if (e.hash == key){
                    return e.value;
                }
                e = e.next;
            }
        }
        return -1;
    }

    public boolean containsKey(long key){
        if (count != 0){ // read-volatile
            HashEntry e = getFirst(key);
            while (e != null){
                if (e.hash == key)
                    return true;
                e = e.next;
            }
        }
        return false;
    }

    public boolean replace(long key, int oldValue, int newValue){
        synchronized (this){
            HashEntry e = getFirst(key);
            while (e != null && (e.hash != key))
                e = e.next;

            boolean replaced = false;
            if (e != null && oldValue == e.value){
                replaced = true;
                e.value = newValue;
            }
            return replaced;
        }
    }

    public int replace(long key, int newValue){
        synchronized (this){
            HashEntry e = getFirst(key);
            while (e != null && (e.hash != key))
                e = e.next;

            int oldValue = -1;
            if (e != null){
                oldValue = e.value;
                e.value = newValue;
            }
            return oldValue;
        }
    }

    public int put(long key, int value){
        return put(key, value, false);
    }

    public int putIfAbsent(long key, int value){
        return put(key, value, true);
    }

    public int put(long key, int value, boolean onlyIfAbsent){
        synchronized (this){
            int c = count;
            if (c++ > threshold) // ensure capacity
                rehash();
            HashEntry[] tab = table;
            int index = ((int) key) & (tab.length - 1);
            HashEntry first = tab[index];
            HashEntry e = first;
            while (e != null && (e.hash != key))
                e = e.next;

            int oldValue;
            if (e != null){
                oldValue = e.value;
                if (!onlyIfAbsent)
                    e.value = value;
            } else{
                oldValue = -1;
                tab[index] = new HashEntry(key, first, value);
                count = c; // write-volatile
            }
            return oldValue;
        }
    }

    /**
     * �����key�����value +1. ���֮ǰ���������¼Ӹ�1. ���ԭ���������-1, ����Ϊ0��ɾ�����key
     * @param key
     * @return ���ؼ�����value
     */
    public int increment(long key){
        return increment(key, 1);
    }

    public int increment(long key, int amount){
        synchronized (this){
            int c = count;
            if (c++ > threshold) // ensure capacity
                rehash();
            HashEntry[] tab = table;
            int index = ((int) key) & (tab.length - 1);
            HashEntry first = tab[index];
            HashEntry e = first;
            while (e != null && (e.hash != key))
                e = e.next;

            if (e != null){
                int result = (e.value += amount);
                if (result == 0){
                    // Ҫɾ��

                    // All entries following removed node can stay
                    // in list, but all preceding ones need to be
                    // cloned.
                    HashEntry newFirst = e.next;
                    for (HashEntry p = first; p != e; p = p.next)
                        newFirst = new HashEntry(p.hash, newFirst, p.value);
                    tab[index] = newFirst;
                    count = (c - 2); // write-volatile
                }

                return result;
            } else{
                tab[index] = new HashEntry(key, first, amount);
                count = c; // write-volatile
                return amount;
            }
        }
    }

    /**
     * �����key�����value -1. ���֮ǰ���������¼Ӹ�-1. ���ԭ���������1, ����Ϊ0��ɾ�����key
     * @param key
     * @return ���ؼ�����value
     */
    public int decrement(long key){
        return decrement(key, 1);
    }

    public int decrement(long key, int amount){
        synchronized (this){
            int c = count;
            if (c++ > threshold) // ensure capacity
                rehash();
            HashEntry[] tab = table;
            int index = ((int) key) & (tab.length - 1);
            HashEntry first = tab[index];
            HashEntry e = first;
            while (e != null && (e.hash != key))
                e = e.next;

            if (e != null){
                int result = (e.value -= amount);
                if (result == 0){
                    // Ҫɾ��

                    // All entries following removed node can stay
                    // in list, but all preceding ones need to be
                    // cloned.
                    HashEntry newFirst = e.next;
                    for (HashEntry p = first; p != e; p = p.next)
                        newFirst = new HashEntry(p.hash, newFirst, p.value);
                    tab[index] = newFirst;
                    count = (c - 2); // write-volatile
                }

                return result;
            } else{
                tab[index] = new HashEntry(key, first, -amount);
                count = c; // write-volatile
                return -amount;
            }
        }
    }

    void rehash(){
        HashEntry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity >= MAXIMUM_CAPACITY)
            return;

        /*
         * Reclassify nodes in each list to new Map.  Because we are
         * using power-of-two expansion, the elements from each bin
         * must either stay at same index, or move with a power of two
         * offset. We eliminate unnecessary node creation by catching
         * cases where old nodes can be reused because their next
         * fields won't change. Statistically, at the default
         * threshold, only about one-sixth of them need cloning when
         * a table doubles. The nodes they replace will be garbage
         * collectable as soon as they are no longer referenced by any
         * reader thread that may be in the midst of traversing table
         * right now.
         */

        HashEntry[] newTable = HashEntry.newArray(oldCapacity << 1);
        threshold = (int) (newTable.length * loadFactor);
        int sizeMask = newTable.length - 1;
        for (int i = 0; i < oldCapacity; i++){
            // We need to guarantee that any existing reads of old Map can
            //  proceed. So we cannot yet null out each bin.
            HashEntry e = oldTable[i];

            if (e != null){
                HashEntry next = e.next;
                int idx = ((int) e.hash) & sizeMask;

                //  Single node on list
                if (next == null)
                    newTable[idx] = e;

                else{
                    // Reuse trailing consecutive sequence at same slot
                    HashEntry lastRun = e;
                    int lastIdx = idx;
                    for (HashEntry last = next; last != null; last = last.next){
                        int k = ((int) last.hash) & sizeMask;
                        if (k != lastIdx){
                            lastIdx = k;
                            lastRun = last;
                        }
                    }
                    newTable[lastIdx] = lastRun;

                    // Clone all remaining nodes
                    for (HashEntry p = e; p != lastRun; p = p.next){
                        int k = ((int) p.hash) & sizeMask;
                        HashEntry n = newTable[k];
                        newTable[k] = new HashEntry(p.hash, n, p.value);
                    }
                }
            }
        }
        table = newTable;
    }

    /**
     * Remove; match on key only if value null, else match both.
     */
    public boolean remove(long key){
        synchronized (this){
            int c = count - 1;
            HashEntry[] tab = table;
            int index = ((int) key) & (tab.length - 1);
            HashEntry first = tab[index];
            HashEntry e = first;
            while (e != null && (e.hash != key))
                e = e.next;

            if (e != null){
                // All entries following removed node can stay
                // in list, but all preceding ones need to be
                // cloned.
                HashEntry newFirst = e.next;
                for (HashEntry p = first; p != e; p = p.next)
                    newFirst = new HashEntry(p.hash, newFirst, p.value);
                tab[index] = newFirst;
                count = c; // write-volatile
                return true;
            } else{
                return false;
            }
        }
    }

    public void clear(){
        if (count != 0){
            synchronized (this){
                HashEntry[] tab = table;
                for (int i = tab.length; --i >= 0;){
                    tab[i] = null;
                }
                count = 0; // write-volatile
            }
        }
    }

    static final class HashEntry{
//       final long key;
        final long hash;
        volatile int value;
        final HashEntry next;

        HashEntry(long hash, HashEntry next, int value){
//           this.key = key;
            this.hash = hash;
            this.next = next;
            this.value = value;
        }

        static final HashEntry[] newArray(int i){
            return new HashEntry[i];
        }

        public long getKey(){
            return hash;
        }

        public int getValue(){
            return value;
        }
    }

    transient Set<Long> keySet;
    transient Set<IntValueLongHashMap.Entry> entrySet;

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports element
     * removal, which removes the corresponding mapping from this map,
     * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * <p>The view's <tt>iterator</tt> is a "weakly consistent" iterator
     * that will never throw {@link ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     */
    public Set<Long> keySet(){
        Set<Long> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports element
     * removal, which removes the corresponding mapping from the map,
     * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * <p>The view's <tt>iterator</tt> is a "weakly consistent" iterator
     * that will never throw {@link ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     */
    public Set<IntValueLongHashMap.Entry> entrySet(){
        Set<IntValueLongHashMap.Entry> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }

    /**
     * Returns an enumeration of the keys in this table.
     *
     * @return an enumeration of the keys in this table
     * @see #keySet()
     */
    public Enumeration<Long> keys(){
        return new LongKeyIterator();
    }

    public LongKeyIterator newKeyIte(){
        return new LongKeyIterator();
    }

    public IntValueIterator newValueIte(){
        return new IntValueIterator();
    }

    /* ---------------- Iterator Support -------------- */

    abstract class HashIterator{
        int nextTableIndex;
        HashEntry[] currentTable;
        HashEntry nextEntry;
        HashEntry lastReturned;

        HashIterator(){
            currentTable = table;
            nextTableIndex = currentTable.length - 1;
            advance();
        }

        public void rewind(){
            currentTable = table;
            nextTableIndex = currentTable.length - 1;
            nextEntry = null;
            lastReturned = null;
            advance();
        }

        public boolean hasMoreElements(){
            return hasNext();
        }

        final void advance(){
            if (nextEntry != null && (nextEntry = nextEntry.next) != null)
                return;

            while (nextTableIndex >= 0){
                if ((nextEntry = currentTable[nextTableIndex--]) != null)
                    return;
            }

        }

        public boolean hasNext(){
            return nextEntry != null;
        }

        HashEntry nextEntry(){
            if (nextEntry == null)
                throw new NoSuchElementException();
            lastReturned = nextEntry;
            advance();
            return lastReturned;
        }

        public void remove(){
            if (lastReturned == null)
                throw new IllegalStateException();
            IntValueLongConcurrentHashMap.this.remove(lastReturned.hash);
            lastReturned = null;
        }

        public void cleanUp(){
            lastReturned = null;
            nextEntry = null;
        }
    }

    public final class LongKeyIterator extends HashIterator implements
            ReusableIterator<Long>, Enumeration<Long>{
        public Long next(){
            return super.nextEntry().hash;
        }

        public Long nextElement(){
            return super.nextEntry().hash;
        }

        public long nextLong(){
            return super.nextEntry().hash;
        }
    }

    public final class IntValueIterator extends HashIterator implements
            ReusableIterator<Integer>, Enumeration<Integer>{
        public Integer next(){
            return super.nextEntry().value;
        }

        public Integer nextElement(){
            return super.nextEntry().value;
        }

        public int nextInt(){
            return super.nextEntry().value;
        }
    }

    final class EntryIterator extends HashIterator implements
            ReusableIterator<IntValueLongHashMap.Entry>{
        public IntValueLongHashMap.Entry next(){
            HashEntry e = super.nextEntry();
            return new IntValueLongHashMap.Entry(e.getKey(), e.getValue());
        }
    }

    final class KeySet extends AbstractSet<Long>{
        public ReusableIterator<Long> iterator(){
            return new LongKeyIterator();
        }

        public int size(){
            return IntValueLongConcurrentHashMap.this.size();
        }

        public boolean contains(Object o){
            if (o instanceof Long)
                return IntValueLongConcurrentHashMap.this.containsKey((Long) o);
            return false;
        }

        public boolean remove(Object o){
            if (o instanceof Long)
                return IntValueLongConcurrentHashMap.this.remove((Long) o);
            return false;
        }

        public void clear(){
            IntValueLongConcurrentHashMap.this.clear();
        }
    }

    final class EntrySet extends AbstractSet<IntValueLongHashMap.Entry>{
        public ReusableIterator<IntValueLongHashMap.Entry> iterator(){
            return new EntryIterator();
        }

        public boolean contains(Object o){
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o){
            if (!(o instanceof IntValueLongHashMap.Entry))
                return false;
            IntValueLongHashMap.Entry e = (IntValueLongHashMap.Entry) o;
            return IntValueLongConcurrentHashMap.this.remove(e.getKey());
        }

        public int size(){
            return IntValueLongConcurrentHashMap.this.size();
        }

        public void clear(){
            IntValueLongConcurrentHashMap.this.clear();
        }
    }

}