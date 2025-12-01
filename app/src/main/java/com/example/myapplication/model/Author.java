package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class Author {
    @SerializedName("user_id")
    private String userId="";
    @SerializedName("nickname")
    private String nickname="";
    @SerializedName("avatar")
    private String avatar="";
    public String getUserId() {
        return userId;
    }
    public String getNickname() {
        return nickname;
    }
    public String getAvatar() {
        return avatar;
    }
}
