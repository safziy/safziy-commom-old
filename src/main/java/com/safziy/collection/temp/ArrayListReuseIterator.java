package com.safziy.collection.temp;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

public class ArrayListReuseIterator<E> extends ArrayList<E> implements
        ReuseIteratorCollection<E>{

    public ArrayListReuseIterator(int initialCapacity){
        super(initialCapacity);
    }

    public ArrayListReuseIterator(){
        super();
    }

    @Override
    public ReusableIterator<E> iterator(){
        return new Itr();
    }

    public E getFirst(){
        return get(0);
    }

    private class Itr implements ReusableIterator<E>{

        int cursor = 0;

        int lastRet = -1;

        int expectedModCount = modCount;

        @Override
        public boolean hasNext(){
            return cursor != size();
        }

        @Override
        public E next(){
            checkForComodification();
            try{
                E next = get(cursor);
                lastRet = cursor++;
                return next;
            } catch (IndexOutOfBoundsException e){
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove(){
            if (lastRet == -1)
                throw new IllegalStateException();
            checkForComodification();

            try{
                ArrayListReuseIterator.this.remove(lastRet);
                if (lastRet < cursor)
                    cursor--;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e){
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void rewind(){
            cursor = 0;
            lastRet = -1;
            expectedModCount = modCount;
        }

        final void checkForComodification(){
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

        @Override
        public void cleanUp(){
        }

    }

}
