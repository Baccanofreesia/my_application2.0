package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class Hashtag {
    @SerializedName("start")
    private int start=0;
    @SerializedName("end")
    private int end=0;
    public int getStart() {
        return start;
    }
    public int getEnd() {
        return end;
    }
}
