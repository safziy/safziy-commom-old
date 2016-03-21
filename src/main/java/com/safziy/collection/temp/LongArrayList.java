package com.safziy.collection.temp;

import java.util.Arrays;
import java.util.Collection;
import java.util.RandomAccess;

/**
 * ArrayList<Long>�ı��֣�Ϊlong�Ż�
 * @author java_1
 *
 */
public class LongArrayList implements RandomAccess, Cloneable{
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * The array buffer into which the elements of the ArrayList are stored. The
     * capacity of the ArrayList is the length of this array buffer.
     */
    private transient long[] elementData;

    /**
     * The size of the ArrayList (the number of elements it contains).
     * 
     * @serial
     */
    int size;

    /**
     * Constructs an empty list with the specified initial capacity.
     * 
     * @param initialCapacity
     *            the initial capacity of the list
     * @exception IllegalArgumentException
     *                if the specified initial capacity is negative
     */
    public LongArrayList(int initialCapacity){
        super();
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "
                    + initialCapacity);
        this.elementData = new long[initialCapacity];
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public LongArrayList(){
        this(16);
    }

    /**
     * used by reverseLongArrayList
     * @param holder
     */
    private LongArrayList(boolean holder){
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element. More
     * formally, returns <tt>true</tt> if and only if this list contains at
     * least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     * 
     * @param o
     *            element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    public boolean contains(long o){
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element in
     * this list, or -1 if this list does not contain the element. More
     * formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     */
    public int indexOf(long o){
        for (int i = 0; i < size; i++)
            if (o == elementData[i])
                return i;
        return -1;
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the list's
     * current size. An application can use this operation to minimize the
     * storage of an <tt>ArrayList</tt> instance.
     */
    public void trimToSize(){
        // modCount++;
        int oldCapacity = elementData.length;
        if (size < oldCapacity){
            elementData = Arrays.copyOf(elementData, size);
        }
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if necessary,
     * to ensure that it can hold at least the number of elements specified by
     * the minimum capacity argument.
     * 
     * @param minCapacity
     *            the desired minimum capacity
     */
    public void ensureCapacity(int minCapacity){
        // modCount++;
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity){
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            // minCapacity is usually close to size, so this is a win:
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    }

    /**
     * Returns the number of elements in this list.
     * 
     * @return the number of elements in this list
     */
    public int size(){
        return size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     * 
     * @return <tt>true</tt> if this list contains no elements
     */
    public boolean isEmpty(){
        return size == 0;
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element).
     * 
     * <p>
     * The returned array will be "safe" in that no references to it are
     * maintained by this list. (In other words, this method must allocate a new
     * array). The caller is thus free to modify the returned array.
     * 
     * <p>
     * This method acts as bridge between array-based and collection-based APIs.
     * 
     * @return an array containing all of the elements in this list in proper
     *         sequence
     */
    public long[] toArray(){
        return Arrays.copyOf(elementData, size);
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     * 
     * @param index
     *            index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    public long get(int index){
        RangeCheck(index);

        return elementData[index];
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     * 
     * @param index
     *            index of the element to replace
     * @param element
     *            element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    public long set(int index, long element){
        RangeCheck(index);

        long oldValue = elementData[index];
        elementData[index] = element;
        return oldValue;
    }

    /**
     * Appends the specified element to the end of this list.
     * 
     * @param e
     *            element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(long e){
        ensureCapacity(size + 1); // Increments modCount!!
        elementData[size++] = e;
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     * 
     * @param index
     *            index at which the specified element is to be inserted
     * @param element
     *            element to be inserted
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    public void add(int index, long element){
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);

        ensureCapacity(size + 1); // Increments modCount!!
        System.arraycopy(elementData, index, elementData, index + 1, size
                - index);
        elementData[index] = element;
        size++;
    }

    /**
     * Removes the element at the specified position in this list. Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * 
     * @param index
     *            the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    public long remove(int index){
        RangeCheck(index);

        // modCount++;
        long oldValue = elementData[index];

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index + 1, elementData, index,
                    numMoved);
        elementData[--size] = -1; // Let gc do its work

        return oldValue;
    }

//    public void moveTailToHead(){
//        if (size > 1){
//            elementData[0] = elementData[--size];
//            elementData[size] = -1;
//        }
//    }

    /*
     * Private remove method that skips bounds checking and does not return the
     * value removed.
     */
    private void fastRemove(int index){
        // modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index + 1, elementData, index,
                    numMoved);
        elementData[--size] = -1; // Let gc do its work
    }

    /**
     * Removes all of the elements from this list. The list will be empty after
     * this call returns.
     */
    public void clear(){
        // modCount++;

        // Let gc do its work
//        for (int i = 0; i < size; i++)
//            elementData[i] = -1;

        size = 0;
    }

    /**
     * Removes from this list all of the elements whose index is between
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive. Shifts
     * any succeeding elements to the left (reduces their index). This call
     * shortens the list by <tt>(toIndex - fromIndex)</tt> elements. (If
     * <tt>toIndex==fromIndex</tt>, this operation has no effect.)
     * 
     * @param fromIndex
     *            index of first element to be removed
     * @param toIndex
     *            index after last element to be removed
     * @throws IndexOutOfBoundsException
     *             if fromIndex or toIndex out of range (fromIndex &lt; 0 ||
     *             fromIndex &gt;= size() || toIndex &gt; size() || toIndex &lt;
     *             fromIndex)
     */
    protected void removeRange(int fromIndex, int toIndex){
        // modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

        int newSize = size - (toIndex - fromIndex);
        while (size != newSize)
            elementData[--size] = -1;
    }

    /**
     * Checks if the given index is in range. If not, throws an appropriate
     * runtime exception. This method does *not* check if the index is negative:
     * It is always used immediately prior to an array access, which throws an
     * ArrayIndexOutOfBoundsException if index is negative.
     */
    private void RangeCheck(int index){
        if (index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);
    }

    /**
     * Returns a new IntArrayList backed by this list. The returned list will
     * only support get(int) and size() operation.
     * 
     * @param fromIndex
     * @param toIndex
     * @return
     */
    public LongArrayList subList(int fromIndex, int toIndex){
        subListRangeCheck(fromIndex, toIndex, size);
        return new LongArraySubList(this, 0, fromIndex, toIndex);
    }

    public LongArrayList slice(int fromIndex, int toIndex){
        subListRangeCheck(fromIndex, toIndex, size);
        return new LongArraySubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size){
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex
                    + ") > toIndex(" + toIndex + ")");
    }

    public void reverse(){
        for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--)
            swap(i, j);
    }

    private void swap(int i, int j){
        set(i, set(j, get(i)));
    }

    public LongArrayList reverseList(){
        return new ReverseLongArrayList(this);
    }

    public LongArrayList getOuter(){
        return this;
    }

    /**
     * Returns a virtually reverse list without creating a clone and calls
     * reverse. This does not change the original list.
     * Note that any change to the original list will affects this
     * reverse list
     * 
     * @author java_1
     * 
     */
    private class ReverseLongArrayList extends LongArrayList{
        private final LongArrayList parent;

        public ReverseLongArrayList(LongArrayList parent){
            super(true);
            this.parent = parent;
            this.size = parent.size();
        }

        @Override
        public long get(int index){
            return parent.get(size - index - 1);
        }

        @Override
        public int size(){
            return size;
        }

        @Override
        public int indexOf(long o){
            for (int i = size; --i >= 0;){
                if (o == parent.get(i)){
                    return size - i - 1;
                }
            }
            return -1;
        }

        @Override
        public boolean add(long index){
            throw new UnsupportedOperationException();
        }

        @Override
        public long set(int index, long element){
            throw new UnsupportedOperationException();
        }

        @Override
        public long remove(int index){
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, long element){
            throw new UnsupportedOperationException();
        }

        @Override
        public void reverse(){
            throw new UnsupportedOperationException();
        }

        /**
         * The reverse list of a reverse list is the original list
         */
        @Override
        public LongArrayList reverseList(){
            return getOuter();
        }

        @Override
        public LongArrayList subList(int fromIndex, int toIndex){
            subListRangeCheck(fromIndex, toIndex, size());
            return new LongArraySubList(this, 0, fromIndex, toIndex);
        }

        public LongArrayList slice(int fromIndex, int toIndex){
            subListRangeCheck(fromIndex, toIndex, size());
            return new LongArraySubList(this, 0, fromIndex, toIndex);
        }
    }

    private class LongArraySubList extends LongArrayList{
        private int offset;
        private final LongArrayList parent;

        public LongArraySubList(LongArrayList parent, int offset,
                int fromIndex, int toIndex){
            this.parent = parent;
            this.offset = fromIndex;
            this.size = toIndex - fromIndex;
        }

        @Override
        public long get(int index){
            return parent.get(index + offset);
        }

        @Override
        public int size(){
            return size;
        }

        @Override
        public LongArrayList subList(int fromIndex, int toIndex){
            subListRangeCheck(fromIndex, toIndex, size);
            return new LongArraySubList(this, offset, fromIndex, toIndex);
        }

        public LongArrayList slice(int fromIndex, int toIndex){
            subListRangeCheck(fromIndex, toIndex, size);
            offset += fromIndex;
            this.size = toIndex - fromIndex;
            return this;
        }

        @Override
        public boolean add(long index){
            throw new UnsupportedOperationException();
        }

        @Override
        public long set(int index, long element){
            throw new UnsupportedOperationException();
        }

        @Override
        public long remove(int index){
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, long element){
            throw new UnsupportedOperationException();
        }

        @Override
        public void reverse(){
            throw new UnsupportedOperationException();
        }

        @Override
        public LongArrayList getOuter(){
            return parent;
        }
    }
}
