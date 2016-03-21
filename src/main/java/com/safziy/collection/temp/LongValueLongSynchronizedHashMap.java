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
public class LongValueLongSynchronizedHashMap{
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
    transient volatile Entry[] table;

    /**
     * The load factor for the hash table.  Even though this value
     * is same for all segments, it is replicated to avoid needing
     * links to outer object.
     * @serial
     */
    final float loadFactor;

    public LongValueLongSynchronizedHashMap(int initialCapacity, float lf){
        loadFactor = lf;

        int capacity = 1;
        while (capacity < initialCapacity){
            capacity <<= 1;
        }

        setTable(Entry.newArray(capacity));
    }

    public LongValueLongSynchronizedHashMap(int initialCapacity){
        this(initialCapacity, 0.75f);
    }

    public LongValueLongSynchronizedHashMap(){
        this(16, 0.75f);
    }

    /**
     * Sets table to new Entry array.
     * Call only while holding lock or in constructor.
     */
    void setTable(Entry[] newTable){
        threshold = (int) (newTable.length * loadFactor);
        table = newTable;
    }

    /**
     * Returns properly casted first entry of bin for given hash.
     */
    Entry getFirst(long hash){
        Entry[] tab = table;
        return tab[((int) hash) & (tab.length - 1)];
    }

    /* Specialized implementations of map methods */

    public int size(){
        return count;
    }

    public long get(long key){
        if (count != 0){ // read-volatile
            Entry e = getFirst(key);
            while (e != null){
                if (e.hash == key){
                    return e.value;
                }
                e = e.next;
            }
        }
        return -1;
    }

    private Entry getEntry(long key){
        if (count != 0){ // read-volatile
            Entry e = getFirst(key);
            while (e != null){
                if (e.hash == key){
                    return e;
                }
                e = e.next;
            }
        }
        return null;
    }

    public boolean containsKey(long key){
        if (count != 0){ // read-volatile
            Entry e = getFirst(key);
            while (e != null){
                if (e.hash == key){
                    return true;
                }
                e = e.next;
            }
        }
        return false;
    }

    public boolean replace(long key, long oldValue, long newValue){
        synchronized (this){
            Entry e = getFirst(key);
            while (e != null && (e.hash != key)){
                e = e.next;
            }

            boolean replaced = false;
            if (e != null && oldValue == e.value){
                replaced = true;
                e.value = newValue;
            }
            return replaced;
        }
    }

    public long replace(long key, long newValue){
        synchronized (this){
            Entry e = getFirst(key);
            while (e != null && (e.hash != key)){
                e = e.next;
            }

            long oldValue = -1;
            if (e != null){
                oldValue = e.value;
                e.value = newValue;
            }
            return oldValue;
        }
    }

    public long put(long key, long value){
        return put(key, value, false);
    }

    public long putIfAbsent(long key, long value){
        return put(key, value, true);
    }

    public long put(long key, long value, boolean onlyIfAbsent){
        synchronized (this){
            int c = count;
            if (c++ > threshold){
                rehash();
            }
            Entry[] tab = table;
            int index = ((int) key) & (tab.length - 1);
            Entry first = tab[index];
            Entry e = first;
            while (e != null && (e.hash != key)){
                e = e.next;
            }

            long oldValue;
            if (e != null){
                oldValue = e.value;
                if (!onlyIfAbsent){
                    e.value = value;
                }
            } else{
                oldValue = -1;
                tab[index] = new Entry(key, first, value);
                count = c; // write-volatile
            }
            return oldValue;
        }
    }

    public long increment(long key, long amount){
        if (amount <= 0){
            throw new IllegalArgumentException(
                    "LongValueLongConcurrentHashMap.increment��amount�������0: "
                            + amount);
        }

        synchronized (this){
            int c = count;
            if (c++ > threshold){
                rehash();
            }
            Entry[] tab = table;
            int index = ((int) key) & (tab.length - 1);
            Entry first = tab[index];
            Entry e = first;
            while (e != null && (e.hash != key)){
                e = e.next;
            }

            if (e != null){
                return e.value += amount;
            } else{
                tab[index] = new Entry(key, first, amount);
                count = c; // write-volatile
                return amount;
            }
        }
    }

    void rehash(){
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity >= MAXIMUM_CAPACITY){
            return;
        }

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

        Entry[] newTable = Entry.newArray(oldCapacity << 1);
        threshold = (int) (newTable.length * loadFactor);
        int sizeMask = newTable.length - 1;
        for (int i = 0; i < oldCapacity; i++){
            // We need to guarantee that any existing reads of old Map can
            //  proceed. So we cannot yet null out each bin.
            Entry e = oldTable[i];

            if (e != null){
                Entry next = e.next;
                int idx = ((int) e.hash) & sizeMask;

                //  Single node on list
                if (next == null){
                    newTable[idx] = e;
                } else{
                    // Reuse trailing consecutive sequence at same slot
                    Entry lastRun = e;
                    int lastIdx = idx;
                    for (Entry last = next; last != null; last = last.next){
                        int k = ((int) last.hash) & sizeMask;
                        if (k != lastIdx){
                            lastIdx = k;
                            lastRun = last;
                        }
                    }
                    newTable[lastIdx] = lastRun;

                    // Clone all remaining nodes
                    for (Entry p = e; p != lastRun; p = p.next){
                        int k = ((int) p.hash) & sizeMask;
                        Entry n = newTable[k];
                        newTable[k] = new Entry(p.hash, n, p.value);
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
            Entry[] tab = table;
            int index = ((int) key) & (tab.length - 1);
            Entry first = tab[index];
            Entry e = first;
            while (e != null && (e.hash != key)){
                e = e.next;
            }

            if (e != null){
                // All entries following removed node can stay
                // in list, but all preceding ones need to be
                // cloned.
                Entry newFirst = e.next;
                for (Entry p = first; p != e; p = p.next){
                    newFirst = new Entry(p.hash, newFirst, p.value);
                }
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
                Entry[] tab = table;
                for (int i = tab.length; --i >= 0;){
                    tab[i] = null;
                }
                count = 0; // write-volatile
            }
        }
    }

    public static final class Entry{
//       final long key;
        final long hash;
        volatile long value;
        final Entry next;

        Entry(long hash, Entry next, long value){
//           this.key = key;
            this.hash = hash;
            this.next = next;
            this.value = value;
        }

        static final Entry[] newArray(int i){
            return new Entry[i];
        }

        public long getKey(){
            return hash;
        }

        public long getValue(){
            return value;
        }
    }

    transient Set<Long> keySet;

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

    public LongValueIterator newValueIte(){
        return new LongValueIterator();
    }

    /* ---------------- Iterator Support -------------- */

    abstract class HashIterator{
        int nextTableIndex;
        Entry[] currentTable;
        Entry nextEntry;
        Entry lastReturned;

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
            if (nextEntry != null && (nextEntry = nextEntry.next) != null){
                return;
            }

            while (nextTableIndex >= 0){
                if ((nextEntry = currentTable[nextTableIndex--]) != null){
                    return;
                }
            }

        }

        public boolean hasNext(){
            return nextEntry != null;
        }

        Entry nextEntry(){
            if (nextEntry == null){
                throw new NoSuchElementException();
            }
            lastReturned = nextEntry;
            advance();
            return lastReturned;
        }

        public void remove(){
            if (lastReturned == null){
                throw new IllegalStateException();
            }
            LongValueLongSynchronizedHashMap.this.remove(lastReturned.hash);
            lastReturned = null;
        }

        public void cleanUp(){
            lastReturned = null;
            nextEntry = null;
        }
    }

    public final class LongKeyIterator extends HashIterator implements
            ReusableIterator<Long>, Enumeration<Long>{
        @Override
        public Long next(){
            return super.nextEntry().hash;
        }

        @Override
        public Long nextElement(){
            return super.nextEntry().hash;
        }

        public long nextLong(){
            return super.nextEntry().hash;
        }
    }

    public final class LongValueIterator extends HashIterator implements
            ReusableIterator<Long>, Enumeration<Long>{
        @Override
        public Long next(){
            return super.nextEntry().value;
        }

        @Override
        public Long nextElement(){
            return super.nextEntry().value;
        }

        public long nextLong(){
            return super.nextEntry().value;
        }
    }

    private transient Set<Entry> entrySet = null;

    public Set<Entry> entrySet(){
        return entrySet0();
    }

    private Set<Entry> entrySet0(){
        Set<Entry> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    public final class EntryIterator extends HashIterator implements
            ReusableIterator<Entry>, Enumeration<Entry>{
        @Override
        public Entry next(){
            return super.nextEntry();
        }

        @Override
        public Entry nextElement(){
            return super.nextEntry();
        }
    }

    private final class EntrySet extends AbstractSet<Entry>{
        @Override
        public ReusableIterator<Entry> iterator(){
            return new EntryIterator();
        }

        @Override
        public boolean contains(Object o){
            if (!(o instanceof Entry)){
                return false;
            }
            Entry e = (Entry) o;

            Entry candidate = getEntry(e.getKey());

            return candidate != null && candidate.getValue() == e.getValue();
        }

        @Override
        public boolean remove(Object o){
            throw new UnsupportedOperationException();
        }

        @Override
        public int size(){
            return count;
        }

        @Override
        public void clear(){
            LongValueLongSynchronizedHashMap.this.clear();
        }
    }

    final class KeySet extends AbstractSet<Long>{
        @Override
        public ReusableIterator<Long> iterator(){
            return new LongKeyIterator();
        }

        @Override
        public int size(){
            return LongValueLongSynchronizedHashMap.this.size();
        }

        @Override
        public boolean contains(Object o){
            if (o instanceof Long){
                return LongValueLongSynchronizedHashMap.this
                        .containsKey((Long) o);
            }
            return false;
        }

        @Override
        public boolean remove(Object o){
            if (o instanceof Long){
                return LongValueLongSynchronizedHashMap.this.remove((Long) o);
            }
            return false;
        }

        @Override
        public void clear(){
            LongValueLongSynchronizedHashMap.this.clear();
        }
    }

}