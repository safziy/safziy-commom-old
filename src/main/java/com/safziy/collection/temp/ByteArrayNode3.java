package com.safziy.collection.temp;

public class ByteArrayNode3 {

    public final byte[] left;
    
    public final byte[] middle;
    
    public final byte[] right;
    
    public final int totalLength;
    
    public ByteArrayNode3(byte[] l, byte[] m, byte[] r){
        if (l == null || r == null){
            throw new NullPointerException();
        }
        this.left = l;
        this.middle = m;
        this.right = r;
        totalLength = l.length + m.length + r.length;
    }
}
