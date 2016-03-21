package com.safziy.collection.temp;


public interface ReuseIteratorCollection<E> {

    public ReusableIterator<E> iterator();
    
    public E getFirst();
}
