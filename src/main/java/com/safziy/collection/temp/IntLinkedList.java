package com.safziy.collection.temp;

import java.util.NoSuchElementException;

/**
 * δ���
 * @author java_1
 *
 */
public class IntLinkedList {

    private transient Entry header = new Entry(0, null, null);
    private transient int size = 0;

    protected transient int modCount = 0;
    public IntLinkedList(){
        header.next = header.previous = header;
    }
    
    public int getFirst(){
        if (size==0)
            throw new NoSuchElementException();

        return header.next.element;
    }
    

    public int getLast()  {
    if (size==0)
        throw new NoSuchElementException();

    return header.previous.element;
    }

    /**
     * Removes and returns the first element from this list.
     *
     * @return the first element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public int removeFirst() {
    return remove(header.next);
    }

    /**
     * Removes and returns the last element from this list.
     *
     * @return the last element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public int removeLast() {
    return remove(header.previous);
    }

    private int remove(Entry e) {
    if (e == header)
        throw new NoSuchElementException();

        int result = e.element;
    e.previous.next = e.next;
    e.next.previous = e.previous;
        e.next = e.previous = null;
        e.element = -1;
    size--;
    modCount++;
        return result;
    }
    
    private static class Entry {
        int element;
        Entry next;
        Entry previous;
    
        Entry(int element, Entry next, Entry previous) {
            this.element = element;
            this.next = next;
            this.previous = previous;
        }
    }
}
