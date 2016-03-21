package com.safziy.collection.temp;

import java.util.Iterator;

public interface ReusableIterator<E> extends Iterator<E>{
    void rewind();

    void cleanUp();
}
