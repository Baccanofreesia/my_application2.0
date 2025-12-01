package com.example.myapplication.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * 动画工具类
 */
public class AnimationUtils {

    /**
     * 点赞缩放动画（类似小红书）
     * @param view 需要添加动画的View
     */
    public static void playLikeAnimation(View view) {
        // 第一阶段：放大到 1.3 倍
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.3f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.3f);
        scaleUpX.setDuration(150);  // 动画时长 150ms
        scaleUpY.setDuration(150);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);

        // 第二阶段：回弹到 1.0 倍（带弹性效果）
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.3f, 1.0f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.3f, 1.0f);
        scaleDownX.setDuration(200);  // 动画时长 200ms
        scaleDownY.setDuration(200);

        // 使用 OvershootInterpolator 实现弹性效果
        scaleDownX.setInterpolator(new OvershootInterpolator(3.0f));
        scaleDownY.setInterpolator(new OvershootInterpolator(3.0f));

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);

        // 组合两个阶段的动画
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(scaleUp, scaleDown);
        animatorSet.start();
    }

    /**
     * 取消点赞缩小动画
     * @param view 需要添加动画的View
     */
    public static void playUnlikeAnimation(View view) {
        // 轻微缩小效果
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.8f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.8f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);

        // 恢复正常大小
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1.0f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1.0f);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(scaleDown, scaleUp);
        animatorSet.start();
    }
}