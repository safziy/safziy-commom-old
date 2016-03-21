package com.safziy.collection.temp;

public class ByteArrayNode2 {

    public final byte[] left;
    
    public final byte[] right;
    
    public final int totalLength;
    
    public ByteArrayNode2(byte[] l, byte[] r){
        if (l == null || r == null){
            throw new NullPointerException();
        }
        this.left = l;
        this.right = r;
        totalLength = l.length + r.length;
    }
}
