package com.safziy.collection.temp;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * 多线程安全, 删除时, 把最后的提到空档处
 * @author Timmy
 *
 */
public class ConcurrentNoOrderArrayList<E> implements Iterable<E>{

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     */
    private transient Object[] elementData;

    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     * @serial
     */
    private int size;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public ConcurrentNoOrderArrayList(int initialCapacity){
        super();
        if (initialCapacity < 0){
            throw new IllegalArgumentException("Illegal Capacity: "
                    + initialCapacity);
        }
        this.elementData = new Object[initialCapacity];
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public ConcurrentNoOrderArrayList(){
        this(10);
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public ConcurrentNoOrderArrayList(Collection<? extends E> c){
        elementData = c.toArray();
        size = elementData.length;
        // c.toArray might (incorrectly) not return Object[] (see 6260652)
        if (elementData.getClass() != Object[].class){
            elementData = Arrays.copyOf(elementData, size, Object[].class);
        }
    }

    private void ensureCapacityInternal(int minCapacity){
        // overflow-conscious code
        if (minCapacity - elementData.length > 0){
            grow(minCapacity);
        }
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity){
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0){
            newCapacity = minCapacity;
        }
        if (newCapacity - MAX_ARRAY_SIZE > 0){
            newCapacity = hugeCapacity(minCapacity);
        }
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity){
        if (minCapacity < 0){
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE
                : MAX_ARRAY_SIZE;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(E e){
        if (e == null){
            throw new NullPointerException();
        }
        synchronized (this){
            ensureCapacityInternal(size + 1);
            elementData[size++] = e;
            return true;
        }
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index){
        synchronized (this){
            rangeCheck(index);

            return elementData(index);
        }
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear(){
        synchronized (this){
            // Let gc do its work
            for (int i = 0; i < size; i++){
                elementData[i] = null;
            }

            size = 0;
        }
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */
    private String outOfBoundsMsg(int index){
        return "Index: " + index + ", Size: " + size;
    }

    /**
     * Checks if the given index is in range.  If not, throws an appropriate
     * runtime exception.  This method does *not* check if the index is
     * negative: It is always used immediately prior to an array access,
     * which throws an ArrayIndexOutOfBoundsException if index is negative.
     */
    private void rangeCheck(int index){
        if (index >= size){
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    @SuppressWarnings("unchecked")
    E elementData(int index){
        return (E) elementData[index];
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    private E remove(int index){
        synchronized (this){
            rangeCheck(index);

            E oldValue = elementData(index);

            int numMoved = size - index - 1;
            if (numMoved > 0){
                elementData[index] = elementData[size - 1];
            }
            elementData[--size] = null; // Let gc do its work

            return oldValue;
        }
    }

    public E remove(E e){
        if (e == null){
            return null;
        }
        synchronized (this){
            for (int i = 0; i < size; i++){
                if (e.equals(elementData[i])){
                    return remove(i);
                }
            }
            return null;
        }
    }

    public int size(){
        synchronized (this){
            return size;
        }
    }

    @Override
    public ReusableIterator<E> iterator(){
        return new Itr();
    }

    private class Itr implements ReusableIterator<E>{
        int cursor;       // index of next element to return
//        int lastRet = -1; // index of last element returned; -1 if no such

        private Itr(){
            cursor = size;
        }

        @Override
        public boolean hasNext(){
            return cursor > 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next(){
            int i = cursor;
            Object[] elementData = ConcurrentNoOrderArrayList.this.elementData;
            if (i > elementData.length){
                throw new NoSuchElementException();
            }
            cursor = i - 1;
            return (E) elementData[i - 1];
//            return (E) elementData[lastRet = i];
        }

        @Override
        public void remove(){
            throw new UnsupportedOperationException();
        }

        @Override
        public void rewind(){
            cursor = size;
//            lastRet = -1;
        }

        @Override
        public void cleanUp(){

        }
    }

}
