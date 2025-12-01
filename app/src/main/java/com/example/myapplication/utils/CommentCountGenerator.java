package com.example.myapplication.utils;

public class CommentCountGenerator {
    public static int generate(String postId) {
        if (postId == null) return 10;
        int hash = Math.abs((postId + "comment").hashCode());
        return 20 + (hash % 200); // 20 - 219
    }
}
