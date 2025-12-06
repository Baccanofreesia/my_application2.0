package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.fragment.MeFragment;
import com.example.myapplication.utils.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainContainerActivity extends AppCompatActivity {
    private static final String TAG = "MainContainer";
    private TextView tvHome,tvMe;
    private HomeFragment homeFragment;
    private MeFragment meFragment;
    private Fragment currentFragment;
    private int currentTabIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        initBottomNavigation();
        homeFragment = new HomeFragment();
        meFragment = new MeFragment();
        // 默认显示首页Fragment
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: savedInstanceState=null, 显示默认首页");
            switchToFragment(homeFragment, 0);
        }
    }
    private void initBottomNavigation() {
        tvHome = findViewById(R.id.tv_home);
        tvMe = findViewById(R.id.tv_me);
        findViewById(R.id.btn_home).setOnClickListener(v->{
            if(currentTabIndex!=0){
                switchToFragment(homeFragment,0);
            }
        });
        findViewById(R.id.btn_me).setOnClickListener(v->{
            if(currentTabIndex!=4){
                switchToFragment(meFragment,4);
            }
        });
    }
    private void switchToFragment(Fragment fragment, int bottomindex) {
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        if(currentFragment!=null){
            transaction.hide(currentFragment);
        }
        if(fragment.isAdded()){
            transaction.show(fragment);
        }else{
            transaction.add(R.id.fragment_container,fragment);
        }
        transaction.commit();
        updateBottomNavigation(bottomindex);
        currentFragment=fragment;
        currentTabIndex=bottomindex;
    }
    private void updateBottomNavigation(int selectedIndex){
        TextView previousTab=getTabByIndex(currentTabIndex);
        TextView newTab=getTabByIndex(selectedIndex);
        if(previousTab!=null&& previousTab != newTab){
            resetTab(previousTab);
        }
        if(newTab!=null){
            activateTab(newTab);
        }
    }
    private TextView getTabByIndex(int index){
        switch (index){
            case 0:
                return tvHome;
            case 4:
                return tvMe;
            default:
                return null;
        }
    }
    private void resetTab(TextView tab){
        tab.setTextColor(getResources().getColor(R.color.gray_text, null));

        tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

    }
    private void activateTab(TextView tab){
        tab.setTextColor(getResources().getColor(R.color.black, null));
        tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
    }
}
