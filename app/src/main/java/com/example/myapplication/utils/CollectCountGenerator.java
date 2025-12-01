package com.example.myapplication.utils;

public class CollectCountGenerator {
    public static int generate(String postId) {
        if (postId == null) return 5;
        int hash = Math.abs((postId + "collect").hashCode());
        return 10 + (hash % 150); // 10 - 159
    }
}

