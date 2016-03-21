package com.safziy.collection.temp;

public class LeftIntPair<R> {

    @SuppressWarnings("unchecked")
    public static <R> LeftIntPair<R>[] newArray(int length){
        return new LeftIntPair[length];
    }

    public final int left;

    public final R right;

    public LeftIntPair(int left, R right){
        this.left = left;
        this.right = right;
    }

}
