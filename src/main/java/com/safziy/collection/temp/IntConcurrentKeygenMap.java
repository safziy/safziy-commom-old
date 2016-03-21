package com.safziy.collection.temp;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.safziy.collection.temp.IntConcurrentKeygenMap.IntKeySetter;

/**
 * Segments are specialized versions of hash tables. This subclasses from
 * ReentrantLock opportunistically, just to simplify some locking and avoid
 * separate construction.
 */
public class IntConcurrentKeygenMap<V extends IntKeySetter> {
	/*
	 * Segments maintain a table of entry lists that are ALWAYS kept in a
	 * consistent state, so can be read without locking. Next fields of nodes
	 * are immutable (final). All list additions are performed at the front of
	 * each bin. This makes it easy to check changes, and also fast to traverse.
	 * When nodes would otherwise be changed, new nodes are created to replace
	 * them. This works well for hash tables since the bin lists tend to be
	 * short. (The average length is less than two for the default load factor
	 * threshold.)
	 * 
	 * Read operations can thus proceed without locking, but rely on selected
	 * uses of volatiles to ensure that completed write operations performed by
	 * other threads are noticed. For most purposes, the "count" field, tracking
	 * the number of elements, serves as that volatile variable ensuring
	 * visibility. This is convenient because this field needs to be read in
	 * many read operations anyway:
	 * 
	 * - All (unsynchronized) read operations must first read the "count" field,
	 * and should not look at table entries if it is 0.
	 * 
	 * - All (synchronized) write operations should write to the "count" field
	 * after structurally changing any bin. The operations must not take any
	 * action that could even momentarily cause a concurrent read operation to
	 * see inconsistent data. This is made easier by the nature of the read
	 * operations in Map. For example, no operation can reveal that the table
	 * has grown but the threshold has not yet been updated, so there are no
	 * atomicity requirements for this with respect to reads.
	 * 
	 * As a guide, all critical volatile reads and writes to the count field are
	 * marked in code comments.
	 */

	private static final long serialVersionUID = 2249069246763182397L;

	/**
	 * The maximum capacity, used if a higher value is implicitly specified by
	 * either of the constructors with arguments. MUST be a power of two <=
	 * 1<<30 to ensure that entries are indexable using ints.
	 */
	static final int MAXIMUM_CAPACITY = 1 << 30;
	/**
	 * The number of elements in this segment's region.
	 */
	transient volatile int count;

	/**
	 * The table is rehashed when its size exceeds this threshold. (The value of
	 * this field is always <tt>(int)(capacity *
	 * loadFactor)</tt>.)
	 */
	transient int threshold;

	/**
	 * The per-segment table.
	 */
	transient volatile HashEntry<V>[] table;

	/**
	 * The load factor for the hash table. Even though this value is same for
	 * all segments, it is replicated to avoid needing links to outer object.
	 * 
	 * @serial
	 */
	final float loadFactor;

	/**
	 * key generator
	 */
	private int keygen;

	private final int startKey;

	private final int maxKey;

	private final Object lockObject = new Object();

	public IntConcurrentKeygenMap(int startKey, int maxKey,
			int initialCapacity, float lf) {
		if (startKey < 0) {
			throw new IllegalArgumentException("start key must be positive.");
		}

		if (maxKey < 0) {
			throw new IllegalArgumentException("max key must be positive.");
		}

		if (startKey >= maxKey) {
			throw new IllegalArgumentException(
					"start key must be less than max key.");
		}
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("Illegal Capacity: "
					+ initialCapacity);
		}
		if (lf <= 0) {
			throw new IllegalArgumentException("Illegal Load: " + lf);
		}

		// Find a power of 2 >= initialCapacity
		int capacity = 1;
		while (capacity < initialCapacity) {
			capacity <<= 1;
		}

		this.startKey = startKey;
		this.maxKey = maxKey;

		keygen = startKey;
		loadFactor = lf;
		setTable(HashEntry.<V> newArray(capacity));
	}

	public IntConcurrentKeygenMap(int startKey, int maxKey, int initialCapacity) {
		this(startKey, maxKey, initialCapacity, 0.75f);
	}

	public IntConcurrentKeygenMap(int startKey, int maxKey) {
		this(startKey, maxKey, 16, 0.75f);
	}

	public IntConcurrentKeygenMap(int startKey) {
		this(startKey, Integer.MAX_VALUE, 16, 0.75f);
	}

	public IntConcurrentKeygenMap() {
		this(0, Integer.MAX_VALUE, 16, 0.75f);
	}

	@SuppressWarnings("unchecked")
	static final <V extends IntKeySetter> IntConcurrentKeygenMap<V>[] newArray(
			int i) {
		return new IntConcurrentKeygenMap[i];
	}

	/**
	 * Sets table to new HashEntry array. Call only while holding lock or in
	 * constructor.
	 */
	void setTable(HashEntry<V>[] newTable) {
		threshold = (int) (newTable.length * loadFactor);
		table = newTable;
	}

	/**
	 * Returns properly casted first entry of bin for given hash.
	 */
	HashEntry<V> getFirst(int hash) {
		HashEntry<V>[] tab = table;
		return tab[hash & (tab.length - 1)];
	}

	/**
	 * Reads value field of an entry under lock. Called if value field ever
	 * appears to be null. This is possible only if a compiler happens to
	 * reorder a HashEntry initialization with its table assignment, which is
	 * legal under memory model but is not known to ever occur.
	 */
	V readValueUnderLock(HashEntry<V> e) {
		synchronized (lockObject) {
			return e.value;
		}
	}

	/* Specialized implementations of map methods */

	public int size() {
		return count;
	}

	public V get(int key) {
		if (count != 0) { // read-volatile
			HashEntry<V> e = getFirst(key);
			while (e != null) {
				if (e.hash == key) {
					V v = e.value;
					if (v != null) {
						return v;
					}
					return readValueUnderLock(e); // recheck
				}
				e = e.next;
			}
		}
		return null;
	}

	public boolean containsKey(int key) {
		if (count != 0) { // read-volatile
			HashEntry<V> e = getFirst(key);
			while (e != null) {
				if (e.hash == key) {
					return true;
				}
				e = e.next;
			}
		}
		return false;
	}

	public boolean containsValue(Object value) {
		if (count != 0) { // read-volatile
			HashEntry<V>[] tab = table;
			int len = tab.length;
			for (int i = 0; i < len; i++) {
				for (HashEntry<V> e = tab[i]; e != null; e = e.next) {
					V v = e.value;
					if (v == null) {
						v = readValueUnderLock(e);
					}
					if (value.equals(v)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean replace(int key, V oldValue, V newValue) {
		synchronized (lockObject) {
			HashEntry<V> e = getFirst(key);
			while (e != null && (e.hash != key)) {
				e = e.next;
			}

			boolean replaced = false;
			if (e != null && oldValue.equals(e.value)) {
				replaced = true;
				e.value = newValue;
			}
			return replaced;
		}
	}

	public V replace(int key, V newValue) {
		synchronized (lockObject) {
			HashEntry<V> e = getFirst(key);
			while (e != null && (e.hash != key)) {
				e = e.next;
			}

			V oldValue = null;
			if (e != null) {
				oldValue = e.value;
				e.value = newValue;
			}
			return oldValue;
		}
	}

	public int add(V value) {
		synchronized (lockObject) {
			int key = keygen++;
			if (key >= maxKey) {
				keygen = startKey;
			}

			value.setKey(key);

			int c = count;
			if (c++ > threshold) {
				rehash();
			}
			HashEntry<V>[] tab = table;
			int index = key & (tab.length - 1);
			HashEntry<V> first = tab[index];

			tab[index] = new HashEntry<V>(key, first, value);
			count = c; // write-volatile
			return key;
		}
	}

	void rehash() {
		HashEntry<V>[] oldTable = table;
		int oldCapacity = oldTable.length;
		if (oldCapacity >= MAXIMUM_CAPACITY) {
			return;
		}

		/*
		 * Reclassify nodes in each list to new Map. Because we are using
		 * power-of-two expansion, the elements from each bin must either stay
		 * at same index, or move with a power of two offset. We eliminate
		 * unnecessary node creation by catching cases where old nodes can be
		 * reused because their next fields won't change. Statistically, at the
		 * default threshold, only about one-sixth of them need cloning when a
		 * table doubles. The nodes they replace will be garbage collectable as
		 * soon as they are no longer referenced by any reader thread that may
		 * be in the midst of traversing table right now.
		 */

		HashEntry<V>[] newTable = HashEntry.newArray(oldCapacity << 1);
		threshold = (int) (newTable.length * loadFactor);
		int sizeMask = newTable.length - 1;
		for (int i = 0; i < oldCapacity; i++) {
			// We need to guarantee that any existing reads of old Map can
			// proceed. So we cannot yet null out each bin.
			HashEntry<V> e = oldTable[i];

			if (e != null) {
				HashEntry<V> next = e.next;
				int idx = e.hash & sizeMask;

				// Single node on list
				if (next == null) {
					newTable[idx] = e;
				} else {
					// Reuse trailing consecutive sequence at same slot
					HashEntry<V> lastRun = e;
					int lastIdx = idx;
					for (HashEntry<V> last = next; last != null; last = last.next) {
						int k = last.hash & sizeMask;
						if (k != lastIdx) {
							lastIdx = k;
							lastRun = last;
						}
					}
					newTable[lastIdx] = lastRun;

					// Clone all remaining nodes
					for (HashEntry<V> p = e; p != lastRun; p = p.next) {
						int k = p.hash & sizeMask;
						HashEntry<V> n = newTable[k];
						newTable[k] = new HashEntry<V>(p.hash, n, p.value);
					}
				}
			}
		}
		table = newTable;
	}

	public V remove(int key) {
		return remove(key, null);
	}

	/**
	 * Remove; match on key only if value null, else match both.
	 */
	public V remove(int key, Object value) {
		synchronized (lockObject) {
			int c = count - 1;
			HashEntry<V>[] tab = table;
			int index = key & (tab.length - 1);
			HashEntry<V> first = tab[index];
			HashEntry<V> e = first;
			while (e != null && (e.hash != key)) {
				e = e.next;
			}

			V oldValue = null;
			if (e != null) {
				V v = e.value;
				if (value == null || value.equals(v)) {
					oldValue = v;
					// All entries following removed node can stay
					// in list, but all preceding ones need to be
					// cloned.
					HashEntry<V> newFirst = e.next;
					for (HashEntry<V> p = first; p != e; p = p.next) {
						newFirst = new HashEntry<V>(p.hash, newFirst, p.value);
					}
					tab[index] = newFirst;
					count = c; // write-volatile
				}
			}
			return oldValue;
		}
	}

	public void clear() {
		if (count != 0) {
			synchronized (lockObject) {
				HashEntry<V>[] tab = table;
				for (int i = tab.length; --i >= 0;) {
					tab[i] = null;
				}
				count = 0; // write-volatile
			}
		}
	}

	static final class HashEntry<V> {
		// final int key;
		final int hash;
		volatile V value;
		final HashEntry<V> next;

		HashEntry(int hash, HashEntry<V> next, V value) {
			// this.key = key;
			this.hash = hash;
			this.next = next;
			this.value = value;
		}

		@SuppressWarnings("unchecked")
		static final <V> HashEntry<V>[] newArray(int i) {
			return new HashEntry[i];
		}

		public int getKey() {
			return hash;
		}

		public V getValue() {
			return value;
		}
	}

	transient Set<Integer> keySet;
	transient Set<IntHashMap.Entry<V>> entrySet;
	transient Collection<V> values;

	/**
	 * Returns a {@link Set} view of the keys contained in this map. The set is
	 * backed by the map, so changes to the map are reflected in the set, and
	 * vice-versa. The set supports element removal, which removes the
	 * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
	 * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
	 * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
	 * <tt>addAll</tt> operations.
	 *
	 * <p>
	 * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will
	 * never throw {@link ConcurrentModificationException}, and guarantees to
	 * traverse elements as they existed upon construction of the iterator, and
	 * may (but is not guaranteed to) reflect any modifications subsequent to
	 * construction.
	 */
	public Set<Integer> keySet() {
		Set<Integer> ks = keySet;
		return (ks != null) ? ks : (keySet = new KeySet());
	}

	/**
	 * Returns a {@link Collection} view of the values contained in this map.
	 * The collection is backed by the map, so changes to the map are reflected
	 * in the collection, and vice-versa. The collection supports element
	 * removal, which removes the corresponding mapping from this map, via the
	 * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support
	 * the <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * <p>
	 * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will
	 * never throw {@link ConcurrentModificationException}, and guarantees to
	 * traverse elements as they existed upon construction of the iterator, and
	 * may (but is not guaranteed to) reflect any modifications subsequent to
	 * construction.
	 */
	public Collection<V> values() {
		return (values != null) ? values : (values = new Values());
	}

	public ReusableIterator<V> reusableValueIterator() {
		return new ValueIterator();
	}

	/**
	 * Returns a {@link Set} view of the mappings contained in this map. The set
	 * is backed by the map, so changes to the map are reflected in the set, and
	 * vice-versa. The set supports element removal, which removes the
	 * corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
	 * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
	 * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
	 * <tt>addAll</tt> operations.
	 *
	 * <p>
	 * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will
	 * never throw {@link ConcurrentModificationException}, and guarantees to
	 * traverse elements as they existed upon construction of the iterator, and
	 * may (but is not guaranteed to) reflect any modifications subsequent to
	 * construction.
	 */
	public Set<IntHashMap.Entry<V>> entrySet() {
		Set<IntHashMap.Entry<V>> es = entrySet;
		return (es != null) ? es : (entrySet = new EntrySet());
	}

	/**
	 * Returns an enumeration of the keys in this table.
	 *
	 * @return an enumeration of the keys in this table
	 * @see #keySet()
	 */
	public Enumeration<Integer> keys() {
		return new KeyIterator();
	}

	/**
	 * Returns an enumeration of the values in this table.
	 *
	 * @return an enumeration of the values in this table
	 * @see #values()
	 */
	public Enumeration<V> elements() {
		return new ValueIterator();
	}

	/* ---------------- Iterator Support -------------- */

	abstract class HashIterator {
		int nextTableIndex;
		HashEntry<V>[] currentTable;
		HashEntry<V> nextEntry;
		HashEntry<V> lastReturned;

		HashIterator() {
			currentTable = table;
			nextTableIndex = currentTable.length - 1;
			advance();
		}

		public void rewind() {
			currentTable = table;
			nextTableIndex = currentTable.length - 1;
			nextEntry = null;
			lastReturned = null;
			advance();
		}

		public boolean hasMoreElements() {
			return hasNext();
		}

		final void advance() {
			if (nextEntry != null && (nextEntry = nextEntry.next) != null) {
				return;
			}

			while (nextTableIndex >= 0) {
				if ((nextEntry = currentTable[nextTableIndex--]) != null) {
					return;
				}
			}

		}

		public boolean hasNext() {
			return nextEntry != null;
		}

		HashEntry<V> nextEntry() {
			if (nextEntry == null) {
				throw new NoSuchElementException();
			}
			lastReturned = nextEntry;
			advance();
			return lastReturned;
		}

		public void remove() {
			if (lastReturned == null) {
				throw new IllegalStateException();
			}
			IntConcurrentKeygenMap.this.remove(lastReturned.hash);
			lastReturned = null;
		}

		public void cleanUp() {
			lastReturned = null;
			nextEntry = null;
		}
	}

	final class KeyIterator extends HashIterator implements
			ReusableIterator<Integer>, Enumeration<Integer> {
		@Override
		public Integer next() {
			return super.nextEntry().hash;
		}

		@Override
		public Integer nextElement() {
			return super.nextEntry().hash;
		}
	}

	final class ValueIterator extends HashIterator implements
			ReusableIterator<V>, Enumeration<V> {
		@Override
		public V next() {
			return super.nextEntry().value;
		}

		@Override
		public V nextElement() {
			return super.nextEntry().value;
		}
	}

	final class EntryIterator extends HashIterator implements
			ReusableIterator<IntHashMap.Entry<V>> {
		@Override
		public IntHashMap.Entry<V> next() {
			HashEntry<V> e = super.nextEntry();
			return new IntHashMap.Entry<V>(e.getKey(), e.getValue());
		}
	}

	final class KeySet extends AbstractSet<Integer> {
		@Override
		public ReusableIterator<Integer> iterator() {
			return new KeyIterator();
		}

		@Override
		public int size() {
			return IntConcurrentKeygenMap.this.size();
		}

		@Override
		public boolean contains(Object o) {
			if (o instanceof Integer) {
				return IntConcurrentKeygenMap.this.containsKey((Integer) o);
			}
			return false;
		}

		@Override
		public boolean remove(Object o) {
			if (o instanceof Integer) {
				return IntConcurrentKeygenMap.this.remove((Integer) o) != null;
			}
			return false;
		}

		@Override
		public void clear() {
			IntConcurrentKeygenMap.this.clear();
		}
	}

	private static Iterator emptyIte = new Iterator() {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Object next() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	};

	final class Values extends AbstractCollection<V> {
		@Override
		public Iterator<V> iterator() {
			if (count == 0) {
				return emptyIte;
			} else {
				return new ValueIterator();
			}
		}

		@Override
		public int size() {
			return IntConcurrentKeygenMap.this.size();
		}

		@Override
		public boolean contains(Object o) {
			return IntConcurrentKeygenMap.this.containsValue(o);
		}

		@Override
		public void clear() {
			IntConcurrentKeygenMap.this.clear();
		}
	}

	final class EntrySet extends AbstractSet<IntHashMap.Entry<V>> {
		@Override
		public ReusableIterator<IntHashMap.Entry<V>> iterator() {
			return new EntryIterator();
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof IntHashMap.Entry)) {
				return false;
			}
			IntHashMap.Entry<?> e = (IntHashMap.Entry<?>) o;
			V v = IntConcurrentKeygenMap.this.get(e.getKey());
			return v != null && v.equals(e.getValue());
		}

		@Override
		public boolean remove(Object o) {
			if (!(o instanceof IntHashMap.Entry)) {
				return false;
			}
			IntHashMap.Entry<?> e = (IntHashMap.Entry<?>) o;
			return IntConcurrentKeygenMap.this.remove(e.getKey(), e.getValue()) != null;
		}

		@Override
		public int size() {
			return IntConcurrentKeygenMap.this.size();
		}

		@Override
		public void clear() {
			IntConcurrentKeygenMap.this.clear();
		}
	}

	public static interface IntKeySetter {
		void setKey(int key);
	}
}