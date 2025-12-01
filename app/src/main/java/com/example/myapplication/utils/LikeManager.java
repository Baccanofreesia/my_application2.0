package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LikeManager extends BaseStateManager {

    private final SharedPreferences sp;

    public LikeManager(Context context) {
        sp = context.getSharedPreferences("like_state", Context.MODE_PRIVATE);
    }

    @Override
    public boolean isActive(String postId) {
        return sp.getBoolean(postId, false);
    }

    @Override
    protected void saveState(String postId, boolean newState) {
        sp.edit().putBoolean(postId, newState).apply();
    }
}
