package com.example.myapplication.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.R;

public class ShareDialog extends Dialog {
    private LinearLayout layoutWechat;
    private LinearLayout layoutMoments;
    private LinearLayout layoutQQ;
    private LinearLayout layoutQzone;
    private ImageView btnClose;
    public ShareDialog(@NonNull Context context){
       super(context, R.style.ShareDialogStyle);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_share);
        //初始化
        initViews();
        //设置窗口属性
        setupWindow();
        //设置点击事件
        setupClickListeners();
    }
    private void initViews(){
        layoutWechat = findViewById(R.id.layout_wechat);
        layoutMoments = findViewById(R.id.layout_moments);
        layoutQQ = findViewById(R.id.layout_qq);
        layoutQzone = findViewById(R.id.layout_qzone);
        btnClose = findViewById(R.id.btn_close);
    }
    private void setupWindow(){
        Window window=getWindow();
        if(window!=null){
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams params=window.getAttributes();
            params.width= WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
            window.setWindowAnimations(R.style.ShareDialogAnimation);

        }
    }
    private void setupClickListeners(){
        layoutWechat.setOnClickListener(v -> {
            // 显示 Toast
            Toast.makeText(getContext(), "分享到：微信好友", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        layoutMoments.setOnClickListener(v -> {
            // 显示 Toast
            Toast.makeText(getContext(), "分享到：朋友圈", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        layoutQQ.setOnClickListener(v -> {
            // 显示 Toast
            Toast.makeText(getContext(), "分享到：QQ", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        layoutQzone.setOnClickListener(v -> {
            // 显示 Toast
            Toast.makeText(getContext(), "分享到：QQ空间", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        btnClose.setOnClickListener(v -> dismiss());
    }

}
