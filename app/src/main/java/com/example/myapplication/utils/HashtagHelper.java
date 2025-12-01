package com.example.myapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.model.Hashtag;

import java.util.List;

/**
 * 话题词处理工具
 */
public class HashtagHelper {

    /**
     * 为TextView设置可点击的话题词
     * @param context 上下文
     * @param textView 目标TextView
     * @param content 正文内容
     * @param hashtags 话题词列表
     */
    public static void setClickableHashtags(Context context, TextView textView, String content, List<Hashtag> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            textView.setText(content);
            return;
        }

        SpannableString spannableString = new SpannableString(content);

        // 为每个话题词设置点击事件
        for (Hashtag hashtag : hashtags) {
            int start = hashtag.getStart();
            int end = hashtag.getEnd();

            // 确保索引有效
            if (start >= 0 && end <= content.length() && start < end) {
                String hashtagText = content.substring(start, end);

                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        // 点击话题词后的操作
                        Toast.makeText(context, "点击了话题：" + hashtagText, Toast.LENGTH_SHORT).show();

                        // TODO: 跳转到话题详情页
                        // Intent intent = new Intent(context, HashtagActivity.class);
                        // intent.putExtra("hashtag", hashtagText);
                        // context.startActivity(intent);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);

                        ds.setColor(Color.parseColor("#1E90FF"));  // 话题词颜色（普蓝色）
                        ds.setUnderlineText(false);  // 去除下划线
                    }
                };

                spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());  // 必须设置才能点击
    }
}