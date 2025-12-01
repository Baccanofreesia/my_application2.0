package com.example.myapplication.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class MeFragment extends Fragment {

    private TextView tvUsername, tvSignature;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);

        // 初始化SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // 初始化视图
        tvUsername = view.findViewById(R.id.tv_username);
        tvSignature = view.findViewById(R.id.tv_signature);

        // 加载用户信息
        loadUserProfile();

        // 设置菜单点击事件
        setupMenuClickListeners(view);

        return view;
    }

    private void loadUserProfile() {
        String username = sharedPreferences.getString("username", "用户名");
        String signature = sharedPreferences.getString("signature", "欢迎来到信息App");

        tvUsername.setText(username);
        tvSignature.setText(signature);
    }

    private void setupMenuClickListeners(View view) {
        view.findViewById(R.id.menu_personal_info).setOnClickListener(v ->
                Toast.makeText(getContext(), "个人信息", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.menu_my_collection).setOnClickListener(v ->
                Toast.makeText(getContext(), "我的收藏", Toast.LENGTH_SHORT).show()
        );

        // ... 其他菜单项
    }
}
