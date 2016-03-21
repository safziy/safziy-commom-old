package com.safziy.collection.temp;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * �̰߳�ȫ����byte[]Ϊkey��hashmap
 * @author java_1
 *
 * @param <V>
 */
public class ByteArrayConcurrentHashMap<V> extends ReentrantLock{

    private static final long serialVersionUID = -7347346540632344374L;

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
    transient volatile HashEntry<V>[] table;

    /**
     * The load factor for the hash table.  Even though this value
     * is same for all segments, it is replicated to avoid needing
     * links to outer object.
     * @serial
     */
    final float loadFactor;

    public ByteArrayConcurrentHashMap(int initialCapacity, float lf){
        if (initialCapacity < 0){
            throw new IllegalArgumentException("Illegal Capacity: "
                    + initialCapacity);
        }
        if (lf <= 0){
            throw new IllegalArgumentException("Illegal Load: " + lf);
        }

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity){
            capacity <<= 1;
        }

        loadFactor = lf;
        setTable(HashEntry.<V> newArray(capacity));
    }

    public ByteArrayConcurrentHashMap(int initialCapacity){
        this(initialCapacity, 0.75f);
    }

    public ByteArrayConcurrentHashMap(){
        this(16, 0.75f);
    }

    @SuppressWarnings("unchecked")
    static final <V> ByteArrayConcurrentHashMap<V>[] newArray(int i){
        return new ByteArrayConcurrentHashMap[i];
    }

    /**
     * Sets table to new HashEntry array.
     * Call only while holding lock or in constructor.
     */
    void setTable(HashEntry<V>[] newTable){
        threshold = (int) (newTable.length * loadFactor);
        table = newTable;
    }

    /**
     * Returns properly casted first entry of bin for given hash.
     */
    HashEntry<V> getFirst(int hash){
        HashEntry<V>[] tab = table;
        return tab[hash & (tab.length - 1)];
    }

    /**
     * Reads value field of an entry under lock. Called if value
     * field ever appears to be null. This is possible only if a
     * compiler happens to reorder a HashEntry initialization with
     * its table assignment, which is legal under memory model
     * but is not known to ever occur.
     */
    V readValueUnderLock(HashEntry<V> e){
        lock();
        try{
            return e.value;
        } finally{
            unlock();
        }
    }

    /* Specialized implementations of map methods */

    public int size(){
        return count;
    }

    public V get(byte[] key){
        if (count != 0){ // read-volatile
            int hash = hash(hashByteArray(key));
            HashEntry<V> e = getFirst(hash);
            while (e != null){
                if (e.hash == hash && Arrays.equals(key, e.key)){
                    V v = e.value;
                    if (v != null){
                        return v;
                    }
                    return readValueUnderLock(e); // recheck
                }
                e = e.next;
            }
        }
        return null;
    }

    public boolean containsKey(byte[] key){
        if (count != 0){ // read-volatile
            int hash = hash(hashByteArray(key));
            HashEntry<V> e = getFirst(hash);
            while (e != null){
                if (e.hash == hash && Arrays.equals(e.key, key)){
                    return true;
                }
                e = e.next;
            }
        }
        return false;
    }

    public boolean containsValue(Object value){
        if (count != 0){ // read-volatile
            HashEntry<V>[] tab = table;
            int len = tab.length;
            for (int i = 0; i < len; i++){
                for (HashEntry<V> e = tab[i]; e != null; e = e.next){
                    V v = e.value;
                    if (v == null){
                        v = readValueUnderLock(e);
                    }
                    if (value.equals(v)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean replace(byte[] key, V oldValue, V newValue){
        lock();
        try{
            int hash = hash(hashByteArray(key));
            HashEntry<V> e = getFirst(hash);
            while (e != null && (e.hash != hash || !Arrays.equals(e.key, key))){
                e = e.next;
            }

            boolean replaced = false;
            if (e != null && oldValue.equals(e.value)){
                replaced = true;
                e.value = newValue;
            }
            return replaced;
        } finally{
            unlock();
        }
    }

    public V replace(byte[] key, V newValue){
        lock();
        try{
            int hash = hash(hashByteArray(key));
            HashEntry<V> e = getFirst(hash);
            while (e != null && (e.hash != hash || !Arrays.equals(e.key, key))){
                e = e.next;
            }

            V oldValue = null;
            if (e != null){
                oldValue = e.value;
                e.value = newValue;
            }
            return oldValue;
        } finally{
            unlock();
        }
    }

    public V put(byte[] key, V value){
        return put(key, value, false);
    }

    public V putIfAbsent(byte[] key, V value){
        if (value == null){
            throw new NullPointerException();
        }
        return put(key, value, true);
    }

    public V put(byte[] key, V value, boolean onlyIfAbsent){
        lock();
        try{
            int c = count;
            if (c++ > threshold){
                rehash();
            }
            HashEntry<V>[] tab = table;
            int hash = hash(hashByteArray(key));
            int index = indexFor(hash, tab.length);

            HashEntry<V> first = tab[index];
            HashEntry<V> e = first;
            while (e != null && (e.hash != hash || !Arrays.equals(e.key, key))){
                e = e.next;
            }

            V oldValue;
            if (e != null){
                oldValue = e.value;
                if (!onlyIfAbsent){
                    e.value = value;
                }
            } else{
                oldValue = null;
                tab[index] = new HashEntry<V>(key, hash, first, value);
                count = c; // write-volatile
            }
            return oldValue;
        } finally{
            unlock();
        }
    }

    void rehash(){
        HashEntry<V>[] oldTable = table;
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

        HashEntry<V>[] newTable = HashEntry.newArray(oldCapacity << 1);
        threshold = (int) (newTable.length * loadFactor);
        int sizeMask = newTable.length - 1;
        for (int i = 0; i < oldCapacity; i++){
            // We need to guarantee that any existing reads of old Map can
            //  proceed. So we cannot yet null out each bin.
            HashEntry<V> e = oldTable[i];

            if (e != null){
                HashEntry<V> next = e.next;
                int idx = e.hash & sizeMask;

                //  Single node on list
                if (next == null){
                    newTable[idx] = e;
                } else{
                    // Reuse trailing consecutive sequence at same slot
                    HashEntry<V> lastRun = e;
                    int lastIdx = idx;
                    for (HashEntry<V> last = next; last != null; last = last.next){
                        int k = last.hash & sizeMask;
                        if (k != lastIdx){
                            lastIdx = k;
                            lastRun = last;
                        }
                    }
                    newTable[lastIdx] = lastRun;

                    // Clone all remaining nodes
                    for (HashEntry<V> p = e; p != lastRun; p = p.next){
                        int k = p.hash & sizeMask;
                        HashEntry<V> n = newTable[k];
                        newTable[k] = new HashEntry<V>(p.key, p.hash, n,
                                p.value);
                    }
                }
            }
        }
        table = newTable;
    }

    public V remove(byte[] key){
        return remove(key, null);
    }

    /**
     * Remove; match on key only if value null, else match both.
     */
    public V remove(byte[] key, Object value){
        lock();
        try{
            int c = count - 1;
            HashEntry<V>[] tab = table;
            int hash = hash(hashByteArray(key));
            int index = indexFor(hash, tab.length);

            HashEntry<V> first = tab[index];
            HashEntry<V> e = first;
            while (e != null && (e.hash != hash || !Arrays.equals(e.key, key))){
                e = e.next;
            }

            V oldValue = null;
            if (e != null){
                V v = e.value;
                if (value == null || value.equals(v)){
                    oldValue = v;
                    // All entries following removed node can stay
                    // in list, but all preceding ones need to be
                    // cloned.
                    HashEntry<V> newFirst = e.next;
                    for (HashEntry<V> p = first; p != e; p = p.next){
                        newFirst = new HashEntry<V>(p.key, p.hash, newFirst,
                                p.value);
                    }
                    tab[index] = newFirst;
                    count = c; // write-volatile
                }
            }
            return oldValue;
        } finally{
            unlock();
        }
    }

    public void clear(){
        if (count != 0){
            lock();
            try{
                HashEntry<V>[] tab = table;
                for (int i = 0; i < tab.length; i++){
                    tab[i] = null;
                }
                count = 0; // write-volatile
            } finally{
                unlock();
            }
        }
    }

    static final class HashEntry<V> {
        final byte[] key;
        final int hash;
        volatile V value;
        final HashEntry<V> next;

        HashEntry(byte[] key, int hash, HashEntry<V> next, V value){
            this.key = key;
            this.hash = hash;
            this.next = next;
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        static final <V> HashEntry<V>[] newArray(int i){
            return new HashEntry[i];
        }

        public byte[] getKey(){
            return key;
        }

        public V getValue(){
            return value;
        }
    }

    /**
     * Applies a supplemental hash function to a given hashCode, which defends
     * against poor quality hash functions. This is critical because HashMap
     * uses power-of-two length hash tables, that otherwise encounter collisions
     * for hashCodes that do not differ in lower bits. Note: Null keys always
     * map to hash 0, thus index 0.
     */
    static int hash(int h){
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    static int hashByteArray(byte[] b){
        return Arrays.hashCode(b);
    }

    /**
     * Returns index for hash code h.
     */
    static int indexFor(int h, int length){
        return h & (length - 1);
    }

    transient Set<byte[]> keySet;
    transient Set<ByteArrayHashMap.Entry<V>> entrySet;
    transient Collection<V> values;

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
    public Set<byte[]> keySet(){
        Set<byte[]> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    public ReusableIterator<V> reusableValueIterator(){
        return new ValueIterator();
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  The collection
     * supports element removal, which removes the corresponding
     * mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt>, and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * <p>The view's <tt>iterator</tt> is a "weakly consistent" iterator
     * that will never throw {@link ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     */
    public Collection<V> values(){
        return (values != null) ? values : (values = new Values());
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
    public Set<ByteArrayHashMap.Entry<V>> entrySet(){
        Set<ByteArrayHashMap.Entry<V>> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }

    /**
     * Returns an enumeration of the keys in this table.
     *
     * @return an enumeration of the keys in this table
     * @see #keySet()
     */
    public Enumeration<byte[]> keys(){
        return new KeyIterator();
    }

    /**
     * Returns an enumeration of the values in this table.
     *
     * @return an enumeration of the values in this table
     * @see #values()
     */
    public Enumeration<V> elements(){
        return new ValueIterator();
    }

    /* ---------------- Iterator Support -------------- */

    abstract class HashIterator{
        int nextTableIndex;
        HashEntry<V>[] currentTable;
        HashEntry<V> nextEntry;
        HashEntry<V> lastReturned;

        HashIterator(){
            currentTable = table;
            nextTableIndex = currentTable.length - 1;
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

        public void rewind(){
            currentTable = table;
            nextTableIndex = currentTable.length - 1;
            advance();
        }

        public boolean hasNext(){
            return nextEntry != null;
        }

        HashEntry<V> nextEntry(){
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
            ByteArrayConcurrentHashMap.this.remove(lastReturned.key);
            lastReturned = null;
        }

        public void cleanUp(){
            lastReturned = null;
            nextEntry = null;
        }
    }

    final class KeyIterator extends HashIterator implements
            ReusableIterator<byte[]>, Enumeration<byte[]>{
        @Override
        public byte[] next(){
            return super.nextEntry().key;
        }

        @Override
        public byte[] nextElement(){
            return super.nextEntry().key;
        }
    }

    final class ValueIterator extends HashIterator implements
            ReusableIterator<V>, Enumeration<V>{
        @Override
        public V next(){
            return super.nextEntry().value;
        }

        @Override
        public V nextElement(){
            return super.nextEntry().value;
        }
    }

    final class EntryIterator extends HashIterator implements
            ReusableIterator<ByteArrayHashMap.Entry<V>>{
        @Override
        public ByteArrayHashMap.Entry<V> next(){
            HashEntry<V> e = super.nextEntry();
            return new ByteArrayHashMap.Entry<V>(e.getKey(), e.getValue());
        }
    }

    final class KeySet extends AbstractSet<byte[]>{
        @Override
        public Iterator<byte[]> iterator(){
            return new KeyIterator();
        }

        @Override
        public int size(){
            return ByteArrayConcurrentHashMap.this.size();
        }

        @Override
        public boolean contains(Object o){
            if (o instanceof byte[]){
                return ByteArrayConcurrentHashMap.this.containsKey((byte[]) o);
            }
            return false;
        }

        @Override
        public boolean remove(Object o){
            if (o instanceof byte[]){
                return ByteArrayConcurrentHashMap.this.remove((byte[]) o) != null;
            }
            return false;
        }

        @Override
        public void clear(){
            ByteArrayConcurrentHashMap.this.clear();
        }
    }

    final class Values extends AbstractCollection<V>{
        @Override
        public Iterator<V> iterator(){
            return new ValueIterator();
        }

        @Override
        public int size(){
            return ByteArrayConcurrentHashMap.this.size();
        }

        @Override
        public boolean contains(Object o){
            return ByteArrayConcurrentHashMap.this.containsValue(o);
        }

        @Override
        public void clear(){
            ByteArrayConcurrentHashMap.this.clear();
        }
    }

    final class EntrySet extends AbstractSet<ByteArrayHashMap.Entry<V>>{
        @Override
        public Iterator<ByteArrayHashMap.Entry<V>> iterator(){
            return new EntryIterator();
        }

        @Override
        public boolean contains(Object o){
            if (!(o instanceof ByteArrayHashMap.Entry)){
                return false;
            }
            ByteArrayHashMap.Entry<?> e = (ByteArrayHashMap.Entry<?>) o;
            V v = ByteArrayConcurrentHashMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }

        @Override
        public boolean remove(Object o){
            if (!(o instanceof ByteArrayHashMap.Entry)){
                return false;
            }
            ByteArrayHashMap.Entry<?> e = (ByteArrayHashMap.Entry<?>) o;
            return ByteArrayConcurrentHashMap.this.remove(e.getKey(),
                    e.getValue()) != null;
        }

        @Override
        public int size(){
            return ByteArrayConcurrentHashMap.this.size();
        }

        @Override
        public void clear(){
            ByteArrayConcurrentHashMap.this.clear();
        }
    }

}
