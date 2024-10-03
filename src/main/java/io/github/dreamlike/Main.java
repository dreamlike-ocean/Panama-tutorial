package io.github.dreamlike;

import io.github.dreamlike.example.NativeImageExample;

public class Main {


    public static void main(String[] args) throws Throwable {
        long[] longs = {1, 2, 3, 4};
        long value = NativeImageExample.getByIndex(longs, 2);
        System.out.println("longs index:2 value:" + value);

        int i = NativeImageExample.capturedLambdaUpcall("123456");

        System.out.println("capturedLambdaUpcall result:" + i);
    }


}