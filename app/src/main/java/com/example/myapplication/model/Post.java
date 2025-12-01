package com.example.myapplication.model;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Post {
    @SerializedName("post_id")
    private String postId="";
    @SerializedName("title")
    private String title= "";
    @SerializedName("content")
    private String content= "";
    @SerializedName("hashtag")
    private List<Hashtag> hashtags=new ArrayList<>();
    @SerializedName("create_time")
    private long createTime;
    @SerializedName("author")
    private Author author;
    @SerializedName("clips")
    private List<Clip> clips=new ArrayList<>();
    @SerializedName("music")
    private Music music;

    public String getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<Hashtag> getHashtags() {
        return hashtags;
    }

    public long getCreateTime() {
        return createTime;
    }

    public Author getAuthor() {
        return author;
    }

    public List<Clip> getClips() {
        return clips;
    }

    public Music getMusic() {
        return music;
    }
}
