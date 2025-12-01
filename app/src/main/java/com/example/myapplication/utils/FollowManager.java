package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class FollowManager extends BaseStateManager {

    private final SharedPreferences sp;

    public FollowManager(Context context) {
        sp = context.getSharedPreferences("follow_state", Context.MODE_PRIVATE);
    }

    @Override
    public boolean isActive(String authorId) {
        return sp.getBoolean(authorId, false);
    }

    @Override
    protected void saveState(String authorId, boolean newState) {
        sp.edit().putBoolean(authorId, newState).apply();
    }
}
