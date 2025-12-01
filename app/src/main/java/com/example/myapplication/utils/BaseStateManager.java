package com.example.myapplication.utils;

public abstract class BaseStateManager {

    /** 是否是激活状态（已点赞/已关注/已收藏） */
    public abstract boolean isActive(String id);

    /** 切换状态，返回切换后的状态 */
    public boolean toggleState(String id) {
        boolean newState = !isActive(id);
        saveState(id, newState);
        onStateChanged(id, newState);
        return newState;
    }

    /** 保存状态（子类实现：可能是 SharedPreferences、数据库等） */
    protected abstract void saveState(String id, boolean newState);

    /** 状态切换后的回调，子类可选 */
    protected void onStateChanged(String id, boolean newState) {}
}

