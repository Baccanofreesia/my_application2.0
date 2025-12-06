package com.example.myapplication.utils;

public abstract class BaseStateManager {

    public abstract boolean isActive(String id);

    public boolean toggleState(String id) {
        boolean newState = !isActive(id);
        saveState(id, newState);
        onStateChanged(id, newState);
        return newState;
    }

    protected abstract void saveState(String id, boolean newState);

    protected void onStateChanged(String id, boolean newState) {}
}

