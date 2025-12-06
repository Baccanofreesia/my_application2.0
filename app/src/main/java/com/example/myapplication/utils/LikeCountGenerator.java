package com.example.myapplication.utils;


public class LikeCountGenerator {

    public static int generateLikeCount(String postId) {
        if (postId == null || postId.isEmpty()) {
            return 100;  // 默认值
        }

        // 使用 postId 的 hashCode 作为种子
        int hash = postId.hashCode();

        // 确保结果是正数
        hash = Math.abs(hash);

        // 映射到 100-999 范围
        return 100 + (hash % 900);
    }

    public static int generateLikeCount(String postId, int min, int max) {
        if (postId == null || postId.isEmpty()) {
            return min;
        }

        int hash = Math.abs(postId.hashCode());
        int range = max - min + 1;

        return min + (hash % range);
    }
}