package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FeedResponse {
    @SerializedName("status_code")
    private int statusCode=0;
    @SerializedName("has_more")
    private int hasMore=0;
    @SerializedName("post_list")
    private List<Post> postList=new ArrayList<>();
    public int getStatusCode(){
        return statusCode;
    }
    public int getHasMore(){
        return hasMore;
    }
    public List<Post> getPostList(){
        return postList;
    }
}
