package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class Clip {
    @SerializedName("type")
    private int type=0;
    @SerializedName("width")
    private int width=0;
    @SerializedName("height")
    private int height=0;
    @SerializedName("url")
    private String url="";
    public int getType() {
        return type;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public String getUrl() {
        return url;
    }
//测试
//    public void setType(int type) {
//        this.type = type;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    public void setWidth(int width) {
//        this.width = width;
//    }
//
//    public void setHeight(int height) {
//        this.height = height;
//    }
}
