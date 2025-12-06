package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class Music {
    @SerializedName("volume")
    private int volume=100;
    @SerializedName("seek_time")
    private int seekTime=0;
    @SerializedName("url")
    private String url="";
    public int getVolume() {
        return volume;
    }
    public int getSeekTime() {
        return seekTime;
    }
    public String getUrl() {
        return url;
    }
//测试
//    public void setUrl(String url) {
//        this.url = url;
//    }
}
