package com.tinyshellzz.InvManager.utils;

public class MyPair<T1, T2> {
    private T1 first;
    private T2 second;
    public MyPair(T1 first, T2 second) {this.first = first; this.second = second;}

    public T1 getKey() {return first;}
    public T2 getValue() {return second;}
}
