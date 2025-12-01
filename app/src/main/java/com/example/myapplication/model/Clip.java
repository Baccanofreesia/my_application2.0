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
}
