package com.safziy.collection.temp;

public final class TripleNodes<T>{

    /** Left object */
    public final T left;

    /** Middle object */
    public final T middle;
    
    /** Right object */
    public final T right;

    /**
     * Create a new instance.
     */
    public TripleNodes(T _left, T _middle, T _right) {
        this.left = _left;
        this.middle = _middle;
        this.right = _right;
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
        builder.append(middle);
        builder.append(",");
        builder.append(right);
        builder.append(")");
        return builder.toString();
    }

    /**
     * Static creation method for a Pair<L, R>.
     * @param <L>
     * @param <R>
     * @param left
     * @param right
     * @return Pair<L, R>(left, right)
     */
    public static <T> TripleNodes<T> of(T left, T middle, T right) {
        return new TripleNodes<T>(left, middle, right);
    }
}
