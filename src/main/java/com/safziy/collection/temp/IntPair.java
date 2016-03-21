package com.safziy.collection.temp;

/**
 * ����Pair, ���Ƿ�int
 * @author java_1
 *
 */
public class IntPair {

    public final int left;
    
    public final int right;
    
    public IntPair(int left, int right){
        this.left = left;
        this.right = right;
    }

    /**
     * Returns a String representation of the Pair in the form: (L,R)
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(left);
        builder.append(",");
        builder.append(right);
        builder.append(")");
        return builder.toString();
    }
    
    public static IntPair of(int left, int right){
        return new IntPair(left, right);
    }
}
