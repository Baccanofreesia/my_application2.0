package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

public class HashtagActivity extends AppCompatActivity {
    private ImageView btnBack;
    private TextView tvHashtagTitle;
    private TextView tvHashtagDesc;
    private RecyclerView recyclerView;
    private String hashtagText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtag);

        // 获取话题词
        getIntentData();

        // 初始化视图
        initViews();


        // 设置数据
        setupData();

        // 设置点击事件
        setupClickListeners();
    }
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvHashtagTitle = findViewById(R.id.tv_hashtag_title);
        tvHashtagDesc = findViewById(R.id.tv_hashtag_desc);
        recyclerView = findViewById(R.id.recycler_view);
    }
    /**
     * 获取 Intent 传递的数据
     */
    private void getIntentData() {
        Intent intent = getIntent();
        hashtagText = intent.getStringExtra("hashtag");

        // 如果没有传递话题词，使用默认值
        if (hashtagText == null || hashtagText.isEmpty()) {
            hashtagText = "#话题";
        }
    }
    /**
     * 设置数据
     */
    private void setupData() {
        // 设置话题标题
        tvHashtagTitle.setText(hashtagText);

        // TODO: 从服务器获取话题相关数据

        // 暂时使用模拟数据
        int postCount = 0; // 模拟数据
        tvHashtagDesc.setText("共 " + postCount + " 篇相关内容");

        // 设置 RecyclerView
        setupRecyclerView();
    }
    private void setupRecyclerView() {
        // 使用 GridLayoutManager（2列）
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        // TODO: 设置适配器

        // 暂时不设置适配器（空列表）
    }
    /**
     * 设置点击事件
     */
    private void setupClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void finish() {
        super.finish();
        // ✅ 在这里设置返回动画
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 设置退出动画（从左到右）
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right);
    }
}
