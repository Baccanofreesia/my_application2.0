package com.example.myapplication.utils;

/**
 * 点赞数生成器
 * 根据作品ID生成固定的伪随机点赞数
 */
public class LikeCountGenerator {

    /**
     * 根据 postId 生成点赞数（100-999之间）
     * 相同的 postId 永远返回相同的点赞数
     *
     * @param postId 作品ID
     * @return 点赞数（100-999）
     */
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

    /**
     * 根据 postId 生成点赞数（自定义范围）
     *
     * @param postId 作品ID
     * @param min 最小值
     * @param max 最大值
     * @return 点赞数
     */
    public static int generateLikeCount(String postId, int min, int max) {
        if (postId == null || postId.isEmpty()) {
            return min;
        }

        int hash = Math.abs(postId.hashCode());
        int range = max - min + 1;

        return min + (hash % range);
    }
}