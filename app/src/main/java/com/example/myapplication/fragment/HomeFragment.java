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
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar pbLoading;
    private PostAdapter adapter;
    private List<Post> posts = new ArrayList<>();

    private boolean isLoading = false;
    private boolean hasMore = true;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 加载Fragment布局
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupLoadMore();
        //首次加载数据
        loadData(true);
        return view;

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
                    // 独立预加载阈值，当滑动到倒数第5个item时，开始预加载后续图片
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
        FeedApi.getFeedList(20,true, new FeedApi.ApiCallback<FeedResponse>() {
            @Override
            public void onSuccess(FeedResponse data){
                isLoading=false;
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                // 缓存加载的数据
                DataManager.getInstance().cachePosts(data.getPostList());
                List<Post> newPosts = data.getPostList();
                if(isRefresh){
                    adapter.refreshData(data.getPostList());
                    preloadImages(0, 5);
                }else{
                    adapter.addData(data.getPostList());
                    // 追加后预加载新批次的前5个帖子的图片
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
    private void preloadImages(int startPosition, int count) {
        if (posts == null || posts.isEmpty()) return;

        int endPosition = Math.min(startPosition + count, posts.size());
        for (int i = startPosition; i < endPosition; i++) {
            Post post = posts.get(i);
            if (post.getClips() != null &&!post.getClips().isEmpty()) {
                String coverUrl = post.getClips().get(0).getUrl();
                // 判断是否为视频
                if (isVideoUrl(coverUrl)) {
                    // 预加载视频第一帧
                    Glide.with(requireContext())
                            .asBitmap()
                            .load(android.net.Uri.parse(coverUrl))
                            .frame(1000000)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .preload();
                } else {
                    // 预加载图片
                    Glide.with(requireContext())
                            .load(coverUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .preload();
                }
                android.util.Log.d("ImagePreload", "预加载帖子 " + i + " 的封面: " + coverUrl);
            }
            // 预加载作者头像
            Author author = post.getAuthor();
            if (author != null && author.getAvatar() != null && !author.getAvatar().isEmpty()) {
                Glide.with(requireContext())
                        .load(author.getAvatar())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload();
            }
        }
    }
    private boolean isVideoUrl(String url) {
        if (url == null) return false;
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".mp4") ||
                lowerUrl.endsWith(".mov") ||
                lowerUrl.endsWith(".avi") ||
                lowerUrl.endsWith(".mkv") ||
                lowerUrl.endsWith(".flv") ||
                lowerUrl.endsWith(".wmv") ||
                lowerUrl.contains("video");
    }
    private void showEmptyState() {
        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_LONG).show();
    }

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