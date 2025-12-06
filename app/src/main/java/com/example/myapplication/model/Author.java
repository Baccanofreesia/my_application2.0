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
   //测试
//    public void setNickname(String 测试君) {
//        this.nickname = 测试君;
//    }
//
//    public void setAvatar(String url) {
//        this.avatar = url;
//    }
}
