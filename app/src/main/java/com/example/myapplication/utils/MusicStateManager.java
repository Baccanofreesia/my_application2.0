package com.example.myapplication.utils;

import com.example.myapplication.model.Music;

public class MusicStateManager {
    private static MusicStateManager instance;
    private boolean isMuted=false;
    private MusicStateManager(){}
    public static MusicStateManager getInstance(){
        if(instance==null){
            instance=new MusicStateManager();
        }
        return instance;
    }
    public boolean isMuted(){
        return isMuted;
    }
    public void setMuted(boolean muted){
        this.isMuted=muted;
        android.util.Log.d("MusicStateManager", "静音状态变更: " + (muted ? "🔇 静音" : "🔊 开启"));
    }
    public boolean toggleMuted() {
        isMuted = !isMuted;
        android.util.Log.d("MusicState", "切换静音: " + (isMuted ? "🔇 静音" : "🔊 开启"));
        return isMuted;
    }
}
