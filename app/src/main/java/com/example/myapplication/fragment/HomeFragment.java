package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplication.R;
import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.api.FeedApi;
import com.example.myapplication.model.Author;
import com.example.myapplication.model.FeedResponse;
import com.example.myapplication.model.Post;
import com.example.myapplication.utils.DataManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    //新增-没看懂，需要复盘
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar pbLoading;
//    //新增加载更多与空态页面
//    private ProgressBar pbLoadMore;       // 加载更多Loading
//    private LinearLayout llEmpty;         // 空态页面
    private PostAdapter adapter;
    private List<Post> posts = new ArrayList<>();

    private boolean isLoading = false;
    private boolean hasMore = true;
    //新增是否首次加载
//    private boolean isFirstLoad = true;   // 是否首次加载
    //新增结束
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // ✅ 加载Fragment布局
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupLoadMore();
        //首次加载数据
        loadData(true);
        return view;
//        // 初始化RecyclerView
//        recyclerView = view.findViewById(R.id.recycler_stagger);
//        StaggeredGridLayoutManager layoutManager =
//                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        recyclerView.setLayoutManager(layoutManager);
//
//        // TODO: 设置适配器
//        // recyclerView.setAdapter(yourAdapter);

    }
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_stagger);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        pbLoading = view.findViewById(R.id.pb_loading);
    }
    private void setupRecyclerView() {
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PostAdapter(requireContext(), posts);//requireContext()和getContext()有什么区别
        recyclerView.setAdapter(adapter);
    }
    private void setupSwipeRefresh(){
        swipeRefresh.setOnRefreshListener(()->loadData(true));
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
    }
    private void setupLoadMore(){
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(!isLoading&&hasMore&&dy>0){
                StaggeredGridLayoutManager layoutManager =
                        (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int[] visibleItemPositions = layoutManager.findLastVisibleItemPositions(null);
                    int lastVisiblePosition = getMax(visibleItemPositions);

                    // 当滑动到倒数第3个item时，加载更多
                    if (lastVisiblePosition >= adapter.getItemCount() - 3) {
                        loadData(false);
                    }
                    // 修改：独立预加载阈值，当滑动到倒数第5个item时，开始预加载后续图片
                    if (lastVisiblePosition >= adapter.getItemCount() - 5) {
                        int preloadStart = lastVisiblePosition + 1;
                        preloadImages(preloadStart, 5);  // 预加载后续5个帖子的图片
                    }
                }
            }
        }
    });
    }
    private void loadData(boolean isRefresh){
        if(isLoading)return;
        if(isRefresh){
            if(!swipeRefresh.isRefreshing()){
                pbLoading.setVisibility(View.VISIBLE);
            }
        }
        FeedApi.getFeedList(20,false, new FeedApi.ApiCallback<FeedResponse>() {
            @Override
            public void onSuccess(FeedResponse data){
                isLoading=false;
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                // ✅ 缓存加载的数据
                DataManager.getInstance().cachePosts(data.getPostList());
                List<Post> newPosts = data.getPostList();
                if(isRefresh){
                    adapter.refreshData(data.getPostList());
                    preloadImages(0, 5);
                }else{
                    adapter.addData(data.getPostList());
                    // 新增：追加后预加载新批次的前5个帖子的图片
                    int startPosition = posts.size() - newPosts.size();
                    preloadImages(startPosition, 5);
                }
                if(posts.isEmpty()&&data.getPostList().isEmpty()){
                    showEmptyState();
                }
            }

            @Override
            public void onError(Exception e){
                isLoading=false;
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if(posts.isEmpty()){
                    showEmptyState();
                }else{
                    // 加载更多失败
                    Toast.makeText(
                            requireContext(),
                            "加载失败: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }
    /**
     * 新增：预加载指定范围的帖子封面图片
     * @param startPosition 开始位置
     * @param count 预加载数量
     */
    private void preloadImages(int startPosition, int count) {
        if (posts == null || posts.isEmpty()) return;

        int endPosition = Math.min(startPosition + count, posts.size());
        for (int i = startPosition; i < endPosition; i++) {
            Post post = posts.get(i);
            if (post.getClips() != null &&!post.getClips().isEmpty()) {
                String coverUrl = post.getClips().get(0).getUrl();
                Glide.with(requireContext())
                        .load(coverUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)  // 启用磁盘和内存缓存
                        .preload();
                android.util.Log.d("ImagePreload", "预加载帖子 " + i + " 的封面: " + coverUrl);
            }
            // 可选：预加载作者头像
            Author author = post.getAuthor();
            if (author != null && author.getAvatar() != null && !author.getAvatar().isEmpty()) {
                Glide.with(requireContext())
                        .load(author.getAvatar())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload();
            }
        }
    }
    private void showEmptyState() {
        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_LONG).show();
    }

    /**
     * 获取数组最大值
     */
    private int getMax(int[] array) {
        int max = array[0];
        for (int value : array) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}