package com.example.myapplication.utils;



public class ShareCountGenerator {

    public static int generate(String postId) {
        if (postId == null || postId.isEmpty()) return 1;

        int hash = Math.abs(postId.hashCode());
        return 5 + (hash % 80); // 5 - 84
    }
}
