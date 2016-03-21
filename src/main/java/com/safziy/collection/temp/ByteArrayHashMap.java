package com.safziy.collection.temp;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * keyΪbyte[]��hashmap�����̰߳�ȫ
 * @author java_1
 *
 * @param <T>
 */
public class ByteArrayHashMap<T> {

    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private transient Entry<T> table[];

    private transient int count;

    private int threshold;

    private final float loadFactor;

    transient volatile int modCount;

    public static class Entry<T> {
        final int hash;
        final byte[] key;
        T value;
        Entry<T> next;

        protected Entry(int h, byte[] k, T v, Entry<T> n){
            this.hash = h;
            this.key = k;
            this.value = v;
            this.next = n;
        }

        public Entry(byte[] key, T value){
            this.key = key;
            this.value = value;
            this.hash = 0;
        }

        @Override
        public final boolean equals(Object o){
            if (!(o instanceof Entry)){
                return false;
            }
            Entry e = (Entry) o;
            byte[] k1 = key;
            byte[] k2 = e.key;
            if (k1 == k2 || (k1 != null && Arrays.equals(k1, k2))){
                Object v1 = value;
                Object v2 = e.value;

                if (v1 == v2 || (v1 != null && v1.equals(v2))){
                    return true;
                }
            }
            return false;
        }

        @Override
        public final int hashCode(){
            return hashByteArray(key) ^ (value == null ? 0 : value.hashCode());
        }

        public byte[] getKey(){
            return key;
        }

        public T getValue(){
            return value;
        }
    }

    public ByteArrayHashMap(){
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public ByteArrayHashMap(int initialCapacity){
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public ByteArrayHashMap(int initialCapacity, float loadFactor){
        super();
        if (initialCapacity < 0){
            throw new IllegalArgumentException("Illegal Capacity: "
                    + initialCapacity);
        }
        if (loadFactor <= 0){
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        }

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity){
            capacity <<= 1;
        }

        this.loadFactor = loadFactor;
        table = new Entry[capacity];
        threshold = (int) (capacity * loadFactor);
    }

    /**
     * <p>
     * Returns the number of keys in this hashtable.
     * </p>
     *
     * @return the number of keys in this hashtable.
     */
    public int size(){
        return count;
    }

    /**
     * <p>
     * Tests if this hashtable maps no keys to values.
     * </p>
     *
     * @return <code>true</code> if this hashtable maps no keys to values;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty(){
        return count == 0;
    }

    public boolean contains(Object value){
        if (value == null){
            throw new NullPointerException();
        }

        Entry<T> tab[] = table;
        for (int i = tab.length; i-- > 0;){
            for (Entry<T> e = tab[i]; e != null; e = e.next){
                if (e.value.equals(value)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>
     * Returns <code>true</code> if this HashMap maps one or more keys to this
     * value.
     * </p>
     *
     * <p>
     * Note that this method is identical in functionality to contains (which
     * predates the Map interface).
     * </p>
     *
     * @param value
     *            value whose presence in this HashMap is to be tested.
     * @return boolean <code>true</code> if the value is contained
     * @see java.util.Map
     * @since JDK1.2
     */
    public boolean containsValue(Object value){
        return contains(value);
    }

    /**
     * <p>
     * Tests if the specified object is a key in this hashtable.
     * </p>
     *
     * @param key
     *            possible key.
     * @return <code>true</code> if and only if the specified object is a key in
     *         this hashtable, as determined by the <tt>equals</tt> method;
     *         <code>false</code> otherwise.
     * @see #contains(Object)
     */
    public boolean containsKey(byte[] key){
        return getEntry(key) != null;
    }

    /**
     * Returns the entry associated with the specified key in the HashMap.
     * Returns null if the HashMap contains no mapping for the key.
     */
    final Entry<T> getEntry(byte[] key){
        if (key == null){
            throw new NullPointerException();
        }
        int hash = hash(hashByteArray(key));
        for (Entry<T> e = table[indexFor(hash, table.length)]; e != null; e = e.next){
            byte[] k;
            if (e.hash == hash
                    && ((k = e.key) == key || (key != null && Arrays.equals(
                            key, k)))){
                return e;
            }
        }
        return null;
    }

    public T get(byte[] key){
        if (key == null){
            throw new NullPointerException();
        }
        int hash = hash(hashByteArray(key));
        for (Entry<T> e = table[indexFor(hash, table.length)]; e != null; e = e.next){
            byte[] k;
            if (e.hash == hash && ((k = e.key) == key || Arrays.equals(key, k))){
                return e.value;
            }
        }
        return null;
    }

    public T put(byte[] key, T value){
        if (key == null){
            throw new NullPointerException();
        }
        int hash = hash(hashByteArray(key));
        int i = indexFor(hash, table.length);
        for (Entry<T> e = table[i]; e != null; e = e.next){
            byte[] k;
            if (e.hash == hash && ((k = e.key) == key || Arrays.equals(k, key))){
                T oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        modCount++;
        addEntry(hash, key, value, i);
        return null;
    }

    /**
     * Adds a new entry with the specified key, value and hash code to
     * the specified bucket.  It is the responsibility of this
     * method to resize the table if appropriate.
     *
     * Subclass overrides this to alter the behavior of put method.
     */
    void addEntry(int hash, byte[] key, T value, int bucketIndex){
        Entry<T> e = table[bucketIndex];
        table[bucketIndex] = new Entry<T>(hash, key, value, e);
        if (count++ >= threshold){
            resize(2 * table.length);
        }
    }

    /**
     * Rehashes the contents of this map into a new array with a
     * larger capacity.  This method is called automatically when the
     * number of keys in this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not
     * resize the map, but sets threshold to Integer.MAX_VALUE.
     * This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two;
     *        must be greater than current capacity unless current
     *        capacity is MAXIMUM_CAPACITY (in which case value
     *        is irrelevant).
     */
    void resize(int newCapacity){
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY){
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int) (newCapacity * loadFactor);
    }

    /**
     * Transfers all entries from current table to newTable.
     */
    void transfer(Entry[] newTable){
        Entry[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++){
            Entry<T> e = src[j];
            if (e != null){
                src[j] = null;
                do{
                    Entry<T> next = e.next;
                    int i = indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param  key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public T remove(byte[] key){
        Entry<T> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }

    /**
     * Removes and returns the entry associated with the specified key
     * in the HashMap.  Returns null if the HashMap contains no mapping
     * for this key.
     */
    final Entry<T> removeEntryForKey(byte[] key){
        if (key == null){
            throw new NullPointerException();
        }
        int hash = hash(hashByteArray(key));
        int i = indexFor(hash, table.length);
        Entry<T> prev = table[i];
        Entry<T> e = prev;

        while (e != null){
            Entry<T> next = e.next;
            byte[] k;
            if (e.hash == hash
                    && ((k = e.key) == key || (key != null && Arrays.equals(
                            key, k)))){
                modCount++;
                count--;
                if (prev == e){
                    table[i] = next;
                } else{
                    prev.next = next;
                }
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Special version of remove for EntrySet.
     */
    final Entry<T> removeMapping(Object o){
        if (!(o instanceof Entry)){
            return null;
        }

        Entry<T> entry = (Entry<T>) o;
        byte[] key = entry.key;
        int hash = hash(hashByteArray(key));
        int i = indexFor(hash, table.length);
        Entry<T> prev = table[i];
        Entry<T> e = prev;

        while (e != null){
            Entry<T> next = e.next;
            if (e.hash == hash && e.equals(entry)){
                modCount++;
                count--;
                if (prev == e){
                    table[i] = next;
                } else{
                    prev.next = next;
                }
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    private abstract class HashIterator<E> implements Iterator<E>{
        Entry<T> next;    // next entry to return
        int expectedModCount;   // For fast-fail
        int index;      // current slot
        Entry<T> current; // current entry

        HashIterator(){
            expectedModCount = modCount;
            if (count > 0){ // advance to first entry
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null){
                    ;
                }
            }
        }

        @Override
        public final boolean hasNext(){
            return next != null;
        }

        final Entry<T> nextEntry(){
            if (modCount != expectedModCount){
                throw new ConcurrentModificationException();
            }
            Entry<T> e = next;
            if (e == null){
                throw new NoSuchElementException();
            }

            if ((next = e.next) == null){
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null){
                    ;
                }
            }
            current = e;
            return e;
        }

        @Override
        public void remove(){
            if (current == null){
                throw new IllegalStateException();
            }
            if (modCount != expectedModCount){
                throw new ConcurrentModificationException();
            }
            byte[] k = current.key;
            current = null;
            ByteArrayHashMap.this.removeEntryForKey(k);
            expectedModCount = modCount;
        }

    }

    private final class ValueIterator extends HashIterator<T>{
        @Override
        public T next(){
            return nextEntry().value;
        }
    }

    private final class KeyIterator extends HashIterator<byte[]>{
        @Override
        public byte[] next(){
            return nextEntry().key;
        }
    }

    private final class EntryIterator extends HashIterator<Entry<T>>{
        @Override
        public Entry<T> next(){
            return nextEntry();
        }
    }

    // Subclass overrides these to alter behavior of views' iterator() method
    Iterator<byte[]> newKeyIterator(){
        return new KeyIterator();
    }

    Iterator<T> newValueIterator(){
        return new ValueIterator();
    }

    Iterator<Entry<T>> newEntryIterator(){
        return new EntryIterator();
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear(){
        modCount++;
        Entry[] tab = table;
        for (int i = 0; i < tab.length; i++){
            tab[i] = null;
        }
        count = 0;
    }

    // Views

    private transient Set<Entry<T>> entrySet = null;

    // Views

    /**
     * Each of these fields are initialized to contain an instance of the
     * appropriate view the first time this view is requested.  The views are
     * stateless, so there's no reason to create more than one of each.
     */
    transient volatile Set<byte[]> keySet = null;
    transient volatile Collection<T> values = null;

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     */
    public Set<byte[]> keySet(){
        Set<byte[]> ks = keySet;
        return (ks != null ? ks : (keySet = new KeySet()));
    }

    private final class KeySet extends AbstractSet<byte[]>{
        @Override
        public Iterator<byte[]> iterator(){
            return newKeyIterator();
        }

        @Override
        public int size(){
            return count;
        }

        @Override
        public boolean contains(Object o){
            if (o instanceof byte[]){
                return containsKey((byte[]) o);
            }
            return false;
        }

        @Override
        public boolean remove(Object o){
            if (o instanceof byte[]){
                return ByteArrayHashMap.this.removeEntryForKey((byte[]) o) != null;
            }
            return false;
        }

        @Override
        public void clear(){
            ByteArrayHashMap.this.clear();
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     */
    public Collection<T> values(){
        Collection<T> vs = values;
        return (vs != null ? vs : (values = new Values()));
    }

    private final class Values extends AbstractCollection<T>{
        @Override
        public Iterator<T> iterator(){
            return newValueIterator();
        }

        @Override
        public int size(){
            return count;
        }

        @Override
        public boolean contains(Object o){
            return containsValue(o);
        }

        @Override
        public void clear(){
            ByteArrayHashMap.this.clear();
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    public Set<Entry<T>> entrySet(){
        return entrySet0();
    }

    private Set<Entry<T>> entrySet0(){
        Set<Entry<T>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    private final class EntrySet extends AbstractSet<Entry<T>>{
        @Override
        public Iterator<Entry<T>> iterator(){
            return newEntryIterator();
        }

        @Override
        public boolean contains(Object o){
            if (!(o instanceof Entry)){
                return false;
            }
            Entry<T> e = (Entry<T>) o;
            Entry<T> candidate = getEntry(e.key);
            return candidate != null && candidate.equals(e);
        }

        @Override
        public boolean remove(Object o){
            return removeMapping(o) != null;
        }

        @Override
        public int size(){
            return count;
        }

        @Override
        public void clear(){
            ByteArrayHashMap.this.clear();
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
}
