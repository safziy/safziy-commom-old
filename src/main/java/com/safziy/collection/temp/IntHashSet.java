package com.safziy.collection.temp;

import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import com.safziy.collection.temp.IntHashMap.Entry;




public class IntHashSet extends AbstractCollection<Integer>{

    private transient IntHashMap<Object> map;
    
    private static final Object PRESENT = new Object();
    
    public IntHashSet(){
        map = new IntHashMap<Object>();
    }
    

    public IntHashSet(int initialCapacity, float loadFactor){
        map = new IntHashMap<Object>(initialCapacity, loadFactor);
    }
    

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality)
     */
    public int size() {
    return map.size();
    }
    /**
     * Returns an iterator over the elements in this set.  The elements
     * are returned in no particular order.
     *
     * @return an Iterator over the elements in this set
     * @see ConcurrentModificationException
     */
    public Iterator<Integer> iterator() {
    return map.keySet().iterator();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this set
     * contains an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this set is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     */
    public boolean contains(int o) {
    return map.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * More formally, adds the specified element <tt>e</tt> to this set if
     * this set contains no element <tt>e2</tt> such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns <tt>false</tt>.
     *
     * @param e element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     * element
     */
    public boolean add(int e) {
    return map.put(e, PRESENT)==null;
    }

    /**
     * Removes the specified element from this set if it is present.
     * More formally, removes an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>,
     * if this set contains such an element.  Returns <tt>true</tt> if
     * this set contained the element (or equivalently, if this set
     * changed as a result of the call).  (This set will not contain the
     * element once the call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return <tt>true</tt> if the set contained the specified element
     */
    public boolean remove(int o) {
    return map.remove(o)==PRESENT;
    }
    /**
     * Removes all of the elements from this set.
     * The set will be empty after this call returns.
     */
    public void clear() {
    map.clear();
    }
    
    public boolean isEmpty() {
    return size() == 0;
    }
    
    @Override
    public boolean equals(Object obj){
        if (obj instanceof IntHashSet){
            IntHashSet set = (IntHashSet)obj;
            if (set.size() == this.size()){
                for (Entry<Object> entry : map.entrySet()){
                    if (!set.contains(entry.getKey())){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode(){
        int result = 0;
        for (Entry<Object> entry : map.entrySet()){
            result += entry.getKey();
        }
        return result;
    }
    

    public double getMapCollisionLevel(){
        return map.getMapCollisionLevel();
    }
}
