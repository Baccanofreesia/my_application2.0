package com.example.myapplication.utils;

import android.util.LruCache;

import com.example.myapplication.model.Author;
import com.example.myapplication.model.Clip;
import com.example.myapplication.model.Music;
import com.example.myapplication.model.Post;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager instance;

    // 使用 LruCache，自动管理内存，超出限制自动移除最久未使用的数据
    // 设置最大缓存数量为 100 个 Post 对象
    private LruCache<String, Post> postCache;

    private DataManager() {
        // 计算缓存大小：假设每个 Post 对象约 10KB
        // 这里设置最大缓存为 100 个对象
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        postCache = new LruCache<String, Post>(cacheSize) {
            @Override
            protected int sizeOf(String key, Post value) {
                // ✅ 估算每个 Post 对象的大小（KB）
                return estimatePostSize(value);
            }
        };
    }
    // 估算 Post 对象大小
    private int estimatePostSize(Post post) {
        int size = 1; // 基础大小 1KB

        // 累加文本长度
        if (post.getTitle() != null) {
            size += post.getTitle().length() / 1024;
        }
        if (post.getContent() != null) {
            size += post.getContent().length() / 1024;
        }

        // 累加图片 URL 数量（每个 Clip 约 0.5KB）
        if (post.getClips() != null) {
            size += post.getClips().size() / 2;
        }

        return Math.max(1, size); // 最小 1KB
    }
    public static DataManager getInstance() {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }

    // 缓存单个 Post
    public void cachePost(Post post) {
        if (post != null && post.getPostId() != null) {
            postCache.put(post.getPostId(), post);
        }
    }

    // 批量缓存（在 HomeFragment 加载数据后调用）
    public void cachePosts(List<Post> posts) {
        if (posts != null) {
            for (Post post : posts) {
                cachePost(post);
            }
        }
    }

    // 获取 Post
    public Post getPostById(String postId) {
        //测试数据
//        if ("TEST_VIDEO".equals(postId)) {
//            Post test = new Post();
//            test.setPostId("TEST_VIDEO");
//            test.setTitle("视频播放测试（混合图文+背景音乐）");
//            test.setContent("这是一条测试作品，包含视频、图片和背景音乐 #测试");
//
//            Author author = new Author();
//            author.setNickname("测试君");
//            author.setAvatar("https://lf3-static.bytednsdoc.com/obj/eden-cn/kndeh7nuvkuhbnbd/test/avatar.jpg");
//            test.setAuthor(author);
//
//            List<Clip> clips = new ArrayList<>();
//
//            // 第一条：视频（会自动决定容器高度）
//            Clip videoClip = new Clip();
//            videoClip.setType(1);
//            videoClip.setUrl("file:///android_asset/test_video.mp4");
//            // 下面这行很重要！不写宽高会走 fallback 3:4
//            videoClip.setWidth(1280);
//            videoClip.setHeight(720);
//            clips.add(videoClip);
//
//            // 第二条：图片
//            Clip imgClip = new Clip();
//            imgClip.setType(0);
//            imgClip.setUrl("https://lf3-static.bytednsdoc.com/obj/eden-cn/kndeh7nuvkuhbnbd/test/cobblestone.jpg");
//            imgClip.setWidth(800);
//            imgClip.setHeight(600);
//            clips.add(imgClip);
//
//            test.setClips(clips);
//
//            // 背景音乐（可选）
//            Music music = new Music();
//            music.setUrl("file:///android_asset/bgm.mp3");
//            test.setMusic(music);
//
//            return test;
//        }

        return postCache.get(postId);
    }

    // 手动清理（可选）
    public void clearCache() {
        postCache.evictAll();
    }

    // 获取缓存统计信息（调试用）
    public String getCacheStats() {
        return "缓存命中率: " + postCache.hitCount() + "/" +
                (postCache.hitCount() + postCache.missCount());
    }
}