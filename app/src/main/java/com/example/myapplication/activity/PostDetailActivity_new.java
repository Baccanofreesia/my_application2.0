package com.example.myapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplication.R;
import com.example.myapplication.adapter.ClipPagerAdapter;
import com.example.myapplication.adapter.ImagePagerAdapter;
import com.example.myapplication.dialog.ShareDialog;
import com.example.myapplication.model.Author;
import com.example.myapplication.model.Clip;
import com.example.myapplication.model.Hashtag;
import com.example.myapplication.model.Post;
import com.example.myapplication.model.Music;
import com.example.myapplication.utils.AnimationUtils;
import com.example.myapplication.utils.CollectCountGenerator;
import com.example.myapplication.utils.CommentCountGenerator;
import com.example.myapplication.utils.DataManager;
import com.example.myapplication.utils.DateFormatter;
import com.example.myapplication.utils.FollowManager;
import com.example.myapplication.utils.LikeCountGenerator;
import com.example.myapplication.utils.LikeManager;
import com.example.myapplication.utils.MusicStateManager;
import com.example.myapplication.utils.ShareCountGenerator;

import java.util.List;
import android.os.Handler;

/**
 * 作品详情页
 */
public class PostDetailActivity_new extends AppCompatActivity {
    private ImageView btnBack;
    private ImageView ivAvatar;
    private TextView tvAuthorNickname;
    private  TextView btnFollow;
    //图片横滑区
    private ViewPager2 viewPagerImages;
    private LinearLayout layoutIndicator;
    private ProgressBar pbImageLoading;
    private LinearLayout layoutImageError;
    private ImageView ivVolume;
    //内容区
    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvDate;
    //底部交互区
    private TextView etComment;
    private LinearLayout layoutLike;
    private LinearLayout layoutComment;
    private LinearLayout layoutCollect;
    private LinearLayout layoutShare;
    private ImageView ivLike;
    private ImageView ivComment;
    private TextView tvLikeCount;
    private TextView tvCommentCount;
    private TextView tvCollectCount;
    private TextView tvShareCount;
    //数据
    private Post post;
    private LikeManager likeManager;
    private FollowManager followManager;
    private MusicStateManager musicStateManager;

    private boolean hasMusic = false;  // ✅ 标记是否有音频,这个有什么用？
    private MediaPlayer mediaPlayer;
    // 自动轮播相关
    private Handler autoPlayHandler;
    private Runnable autoPlayRunnable;
    private static final long AUTO_PLAY_INTERVAL = 3000;  // 3秒切换
    private boolean isAutoPlaying = false;  // 是否正在自动轮播
    private boolean isUserScrolling = false;  // 用户是否在手动滑动
    private int totalImageCount = 0;  // 图片总数
    // 图片加载状态管理
    private ClipPagerAdapter adapter;
    private boolean isCurrentImageLoaded = false;  // 当前图片是否加载完成
    private int maxLoadWaitTime = 5000;  // 最大等待时间 5 秒（避免永久等待）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_detail);
        likeManager = new LikeManager(this);
        followManager = new FollowManager(this);
        musicStateManager = MusicStateManager.getInstance();  //  获取全局状态管理器
        // 初始化自动轮播 Handler
        autoPlayHandler = new Handler(Looper.getMainLooper());
        loadIntentData();
        initViews();
        bindDataToUI();
        setupClickListeners();
    }
    private void initViews(){
        btnBack = findViewById(R.id.btn_back);
        btnFollow = findViewById(R.id.btn_follow);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvAuthorNickname = findViewById(R.id.tv_author_nickname);

        tvTitle = findViewById(R.id.tv_title);
        tvContent = findViewById(R.id.tv_content);
        tvDate = findViewById(R.id.tv_date);

        viewPagerImages = findViewById(R.id.viewpager_images);
        pbImageLoading = findViewById(R.id.pb_image_loading);
        layoutIndicator = findViewById(R.id.layout_indicator);

        ivVolume = findViewById(R.id.iv_volume);

        layoutLike = findViewById(R.id.layout_like);
        layoutCollect = findViewById(R.id.layout_collect);
        layoutComment = findViewById(R.id.layout_comment);
        layoutShare = findViewById(R.id.layout_share);  // ✅ 添加这行
        ivLike = findViewById(R.id.iv_like);

        tvLikeCount = findViewById(R.id.tv_like_count);
        tvCommentCount = findViewById(R.id.tv_comment_count);
        tvCollectCount = findViewById(R.id.tv_collect_count);
        tvShareCount = findViewById(R.id.tv_share_count);
    }
    private void loadIntentData(){
        String postId = getIntent().getStringExtra("post_id");
        // ✅ 添加空值检查
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "作品ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        post = DataManager.getInstance().getPostById(postId);
        if (post == null) {
            // 降级方案：显示加载失败
            Toast.makeText(this, "作品数据已过期", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    private void bindDataToUI(){
        // 作者
        Author author = post.getAuthor();
        if (author != null) {
            tvAuthorNickname.setText(author.getNickname());
            Glide.with(this)
                    .load(author.getAvatar())
                    .circleCrop()
                    .into(ivAvatar);
        }

        // 标题（使用 Post 的便捷方法）
        if (post.getTitle()!=null&& !post.getTitle().isEmpty()) {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(post.getTitle());
        } else {
            tvTitle.setVisibility(View.GONE);
        }

        // 正文 + hashtag 点击
        setContentWithHashtags();

        // 日期
        tvDate.setText(DateFormatter.formatDate(post.getCreateTime()));

        // 图片
        setupClips();
        // 预加载前几张图片
        preloadInitialImages();
        // 检查是否有音频
        Music music = post.getMusic();
        hasMusic = (music != null && music.getUrl() != null && !music.getUrl().isEmpty());
// ✅ 关键修改:只有多图或有音频时才显示音频图标
        if (totalImageCount > 1||hasMusic) {
            ivVolume.setVisibility(View.VISIBLE);
            if (hasMusic) {
                // 有音频:初始化播放器
                initAndPlayMusic(music);
            }

            // 更新UI(有音频显示音量图标,无音频显示播放/暂停图标)
            updateVolumeUI();

            // 如果未静音,开始自动轮播
            if (!musicStateManager.isMuted()) {
                startAutoPlay();
            }

        } else {
            // ✅
            ivVolume.setVisibility(View.GONE);
            hasMusic = false;
        }
//// ✅ 有音频时才初始化并播放
//        Music music = post.getMusic();
//        if (music != null && music.getUrl() != null && !music.getUrl().isEmpty()) {
//            ivVolume.setVisibility(View.VISIBLE);
//            // ✅ 立即设置静音图标（不等异步加载）
////            isMuted = true;
//            hasMusic = true;
//            ivVolume.setVisibility(View.VISIBLE);
//            updateVolumeUI();
//            initAndPlayMusic(music);  // 懒加载
//        } else {
//            hasMusic = false;
//            ivVolume.setVisibility(View.GONE);
//        }

        // 点赞数（来自 LikeCountGenerator）
        int baseLike = LikeCountGenerator.generateLikeCount(post.getPostId());
        boolean liked = likeManager.isActive(post.getPostId());
        tvLikeCount.setText(String.valueOf(liked ? baseLike + 1 : baseLike));
        updateLikeUI(liked);
        boolean  followed = followManager.isActive(post.getPostId());
        updateFollowUI(followed);

        // 评论 & 收藏伪随机（根据 postId）
        tvCommentCount.setText(String.valueOf(CommentCountGenerator.generate(post.getPostId())));
        tvCollectCount.setText(String.valueOf(CollectCountGenerator.generate(post.getPostId())));
        tvShareCount.setText(String.valueOf(ShareCountGenerator.generate(post.getPostId())));
    }
    private void preloadInitialImages() {
        if (post.getClips() == null || post.getClips().isEmpty()) return;

        List<Clip> clips = post.getClips();
        int preloadCount = Math.min(3, clips.size());

        for (int i = 0; i < preloadCount; i++) {
            String imageUrl = clips.get(i).getUrl();
            Glide.with(this)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload();

            android.util.Log.d("ImagePreload", "预加载图片 " + i + ": " + imageUrl);
        }

    }
    /**
     * ✅ 预加载指定位置的图片
     */
    private void preloadNextImage(int position) {
        if (position < 0 || position >= totalImageCount) return;

        List<Clip> clips = post.getClips();
        String imageUrl = clips.get(position).getUrl();

        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload();

        android.util.Log.d("ImagePreload", "预加载下一张图片: " + position);
    }
    // ✅ 懒加载初始化并播放音频
    private void initAndPlayMusic(Music music) {
        // ✅ 先释放旧的 MediaPlayer
        releaseMediaPlayer();
        try {
            android.util.Log.d("MediaPlayer", "📀 初始化音频: " + music.getUrl());
            // 1. 创建 MediaPlayer
            mediaPlayer = new MediaPlayer();
            // ✅ 1. 设置音频流类型（必须在 setDataSource 之前）
            mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            // 2. 设置音频源
            mediaPlayer.setDataSource(music.getUrl());
            // ✅ 根据全局状态设置初始音量
            boolean isMuted = musicStateManager.isMuted();
            if (isMuted) {
                mediaPlayer.setVolume(0f, 0f);
                android.util.Log.d("MediaPlayer", "🔇 初始化为静音");
            } else {
                mediaPlayer.setVolume(1f, 1f);
                android.util.Log.d("MediaPlayer", "🔊 初始化为有声");
            }
//            // 3. 设置音量
//            float volume = music.getVolume() / 100f;  // 假设 volume 是 0-100
//            mediaPlayer.setVolume(volume, volume);
//            // ✅ 默认静音启动
//            mediaPlayer.setVolume(0f, 0f);
            // 4. 设置循环播放
            mediaPlayer.setLooping(true);


            // 6. 准备完成后自动播放
            mediaPlayer.setOnPreparedListener(mp -> {
                if (!isFinishing() && !isDestroyed()) {
                    mp.start();
                    android.util.Log.d("MediaPlayer", "✅ 开始播放");
                }
            });

            // 7. 错误处理
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                android.util.Log.e("MediaPlayer", "播放失败: " + what + ", " + extra);
                Toast.makeText(this, "音频播放失败", Toast.LENGTH_SHORT).show();
                return true;
            });
            // 5. 异步准备（推荐，不会阻塞 UI）
            mediaPlayer.prepareAsync();
            android.util.Log.d("MediaPlayer", "⏳ 开始异步准备...");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "音频加载失败", Toast.LENGTH_SHORT).show();
        }
    }
    private void setContentWithHashtags(){
        String content = post.getContent();
        SpannableString spannableString = new SpannableString(content);
        int index = 0;
        while (index < content.length()) {
            int start = content.indexOf("#", index);
            if (start == -1) break;

            // 找到话题词结束位置（遇到空格、#号或结尾）
            int end = start + 1;
            while (end < content.length()) {
                char c = content.charAt(end);
                // 遇到空格、换行符或另一个#号则结束
                if (Character.isWhitespace(c) || c == '#') {
                    break;
                }
                end++;
            }

            // 提取话题词文本
            final String hashtagText = content.substring(start, end);

            // 设置点击事件
            android.text.style.ClickableSpan clickableSpan = new android.text.style.ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    // 跳转到话题页面
                    openHashtagPage(hashtagText);
                }

                @Override
                public void updateDrawState(@NonNull android.text.TextPaint ds) {
                    super.updateDrawState(ds);

                    ds.setColor(Color.BLUE); // 普蓝色
                    ds.setUnderlineText(false); // 去除下划线
                }
            };

            spannableString.setSpan(clickableSpan, start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = end;
        }

        tvContent.setText(spannableString);
        tvContent.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        // 防止点击话题词时背景高亮
        tvContent.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }
    private void openHashtagPage(String hashtagText) {
        Intent intent = new Intent(this, HashtagActivity.class);
        intent.putExtra("hashtag", hashtagText);
        startActivity(intent);
        // 设置横滑动画（从右到左）
        overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation);
    }

    //kexinf?
    private void setupClips() {
        List<Clip> clips = post.getClips();
        if (clips == null || clips.isEmpty()) return;
        totalImageCount = clips.size();
        List<String> urls = clips.stream().map(Clip::getUrl).collect(java.util.stream.Collectors.toList());
        // 显示加载状态
        pbImageLoading.setVisibility(View.VISIBLE);

        // 动态计算首图比例并设置 ViewPager 高度（关键：必须先设置高度）
        adjustViewPagerHeight(clips.get(0));
        int containerHeight = viewPagerImages.getLayoutParams().height;
        ClipPagerAdapter adapter = new ClipPagerAdapter(this, clips, containerHeight);
//        // ✅ 设置图片加载监听
//        adapter.setOnImageLoadListener(new ImagePagerAdapter.OnImageLoadListener() {
//            @Override
//            public void onImageLoaded(int position, boolean success) {
//                android.util.Log.d("ImageLoad", "图片 " + position + (success ? " ✅ 加载成功" : " ❌ 加载失败"));
//
//                // 只有当前显示的图片加载完成才标记
//                if (position == viewPagerImages.getCurrentItem()) {
//                    isCurrentImageLoaded = success;
//                    android.util.Log.d("AutoPlay", "当前图片 " + position + " 加载状态: " + (success ? "✅" : "❌"));
//                }
//            }
//        });

        viewPagerImages.setAdapter(adapter);
        // 3. 视频回调：自动切换下一段
        adapter.setOnVideoPlayListener(new ClipPagerAdapter.OnVideoPlayListener() {
            @Override
            public void onVideoStart(int position) {
                // 可以更新UI
            }

            @Override
            public void onVideoComplete(int position) {
                int next = (position + 1) % totalImageCount;
                viewPagerImages.setCurrentItem(next, true);
            }
        });
//        // 图片加载完成后隐藏加载状态
//        pbImageLoading.setVisibility(View.GONE);
        // 设置进度条
        if (clips.size() > 1) {
            setupIndicators(clips.size());
            layoutIndicator.setVisibility(View.VISIBLE);
        } else {
            layoutIndicator.setVisibility(View.GONE);
        }
        // ✅ 第一张图片默认已加载（因为已经显示）
        isCurrentImageLoaded = true;
    }
    private void adjustViewPagerHeight(Clip firstClip) {
        // 如果图片列表为空，使用默认高度
        if (post.getClips() == null || post.getClips().isEmpty()) {
            return;
        }
        // 方案1：使用真实图片尺寸（推荐）
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int targetHeight;
        if (firstClip.getWidth() > 0 && firstClip.getHeight() > 0) {
            // 使用真实宽高
            targetHeight = calculateDetailImageHeight(
                    firstClip.getWidth(),
                    firstClip.getHeight(),
                    screenWidth
            );
        } else {
            // 获取不到宽高 → fallback：强制 3:4
            targetHeight = (int) (screenWidth * 4f / 3f);
        }

        ViewGroup.LayoutParams params = viewPagerImages.getLayoutParams();
        params.height = targetHeight;
        viewPagerImages.setLayoutParams(params);

    }
    private int calculateDetailImageHeight(int originalWidth, int originalHeight, int targetWidth) {
        if (originalWidth == 0 || originalHeight == 0) {
            return (int) (targetWidth * 4f / 3f); // 默认 3:4
        }

        float originalRatio = (float) originalWidth / originalHeight;

        // 限制在 3:4 ~ 16:9
        float constrainedRatio = Math.max(3f / 4f, Math.min(16f / 9f, originalRatio));

        return (int) (targetWidth / constrainedRatio);
    }
    private void setupIndicators(int count) {
        layoutIndicator.removeAllViews();

        for (int i = 0; i < count; i++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            );
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundColor(i == 0 ?
                    Color.parseColor("#FFFFFF") :
                    Color.parseColor("#66FFFFFF"));
            layoutIndicator.addView(indicator);
        }
        // ViewPager 切换监听
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }
        });
    }
    /**
     * 更新进度条状态
     */
    private void updateIndicators(int position) {
        for (int i = 0; i < layoutIndicator.getChildCount(); i++) {
            View indicator = layoutIndicator.getChildAt(i);
            indicator.setBackgroundColor(i == position ?
                    Color.parseColor("#FFFFFF") :
                    Color.parseColor("#66FFFFFF"));
        }
    }
    private void updateVolumeUI() {
        boolean isMuted = musicStateManager.isMuted();
        if (isMuted) {
            ivVolume.setImageResource(R.drawable.ic_volume_off);
        } else {
            ivVolume.setImageResource(R.drawable.ic_volume_on);
        }
    }
    private void setupClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());
        layoutLike.setOnClickListener(v -> {
            boolean newState = likeManager.toggleState(post.getPostId());
            updateLikeUI(newState);

            int currentCount = Integer.parseInt(tvLikeCount.getText().toString());

            if (newState) {
                tvLikeCount.setText(String.valueOf(currentCount + 1));
                AnimationUtils.playLikeAnimation(ivLike);
            } else {
                tvLikeCount.setText(String.valueOf(currentCount - 1));
                AnimationUtils.playUnlikeAnimation(ivLike);
            }
        });

        layoutCollect.setOnClickListener(v -> {
            // TODO: 收藏状态管理（你可以加 CollectStateManager）
        });

        layoutComment.setOnClickListener(v -> {
            // TODO: 打开评论界面
        });
        btnFollow.setOnClickListener(v -> {
            boolean newState = followManager.toggleState(post.getPostId());
            updateFollowUI(newState);
        });

        // 音量控制
        ivVolume.setOnClickListener(v -> toggleVolume());
        // 分享
        layoutShare.setOnClickListener(v -> showShareDialog());

    }
    /**
     * 切换音量
     */
//    private void toggleVolume() {
//        if (mediaPlayer == null) return;
//        isMuted = !isMuted;
//
//        if (mediaPlayer != null) {
//            if (isMuted) {
//                mediaPlayer.setVolume(0f, 0f);
//            } else {
//                mediaPlayer.setVolume(1f,1f);
//            }
//        }
//        updateVolumeUI();
//        Toast.makeText(this, isMuted ? "已静音" : "已开启声音", Toast.LENGTH_SHORT).show();
//    }
    private void toggleVolume() {
        // 切换全局状态
        boolean newMutedState = musicStateManager.toggleMuted();

        // 1. 控制音频(如果有)
        if (hasMusic && mediaPlayer != null) {
            try {
                if (newMutedState) {
                    mediaPlayer.setVolume(0f, 0f);
                } else {
                    mediaPlayer.setVolume(1f, 1f);
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }
                }
            } catch (IllegalStateException e) {
                android.util.Log.e("MediaPlayer", "切换音量失败", e);
            }
        }

        // 2. 控制轮播(多图时)
        if (totalImageCount > 1) {
            if (newMutedState) {
                // 静音 = 停止轮播
                stopAutoPlay();

                String msg = hasMusic ? "🔇 已静音并暂停轮播" : "⏸️ 已暂停轮播";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            } else {
                // 开启声音 = 开始轮播
                startAutoPlay();

                String msg = hasMusic ? "🔊 已开启声音并开始轮播" : "▶️ 已开始轮播";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        }

        // 更新 UI
        updateVolumeUI();
    }
    /**
     * 开始自动轮播
     */
    private void startAutoPlay() {
        // ✅ 检查条件：多图 + 未静音
        if (totalImageCount <= 1 || musicStateManager.isMuted()) {
            android.util.Log.d("AutoPlay", "❌ 不满足轮播条件（单图或已静音）");
            return;
        }
        // ✅ 简化：如果已在轮播，直接返回（避免重复启动）
        if (isAutoPlaying) {
            android.util.Log.d("AutoPlay", "⚠️ 轮播已在运行");
            return;
        }
        isAutoPlaying = true;
        android.util.Log.d("AutoPlay", "▶️ 开始自动轮播");

        autoPlayRunnable = new Runnable() {
            private long startWaitTime = 0;  // 记录开始等待的时间
            @Override
            public void run() {
                if (musicStateManager.isMuted() || isUserScrolling || totalImageCount <= 1)  {
                    isAutoPlaying = false;
                    android.util.Log.d("AutoPlay", "⏸️ 检测到停止条件");
                    return;
                }
                if (!isCurrentImageLoaded) {
                    // 初始化等待时间
                    if (startWaitTime == 0) {
                        startWaitTime = System.currentTimeMillis();
                        android.util.Log.d("AutoPlay", "⏳ 开始等待图片加载...");
                    }

                    long waitedTime = System.currentTimeMillis() - startWaitTime;

                    // 如果等待时间超过最大限制，强制切换（避免卡住）
                    if (waitedTime > maxLoadWaitTime) {
                        android.util.Log.w("AutoPlay", "⚠️ 图片加载超时（" + waitedTime + "ms），强制切换");
                        isCurrentImageLoaded = true;  // 强制标记为已加载
                        startWaitTime = 0;
                    } else {
                        // 继续等待，500ms 后再检查
                        android.util.Log.d("AutoPlay", "⏳ 等待中... 已等待 " + waitedTime + "ms");
                        autoPlayHandler.postDelayed(this, 500);
                        return;
                    }
                }

                int currentPosition = viewPagerImages.getCurrentItem();
                int nextPosition = (currentPosition + 1) % totalImageCount;
                android.util.Log.d("AutoPlay", "🔄 切换: " + currentPosition + " → " + nextPosition);

                // ✅ 预加载下一张（提前加载 nextPosition + 1）
                int preloadPosition = (nextPosition + 1) % totalImageCount;
                preloadNextImage(preloadPosition);

                // 切换前重置状态
                isCurrentImageLoaded = false;
                startWaitTime = 0;
                viewPagerImages.setCurrentItem(nextPosition, true);
                autoPlayHandler.postDelayed(this, AUTO_PLAY_INTERVAL);
            }
        };

        autoPlayHandler.postDelayed(autoPlayRunnable, AUTO_PLAY_INTERVAL);
    }

    /**
     * 停止自动轮播
     */
    private void stopAutoPlay() {
        if (!isAutoPlaying) {
            return;
        }

        isAutoPlaying = false;

        if (autoPlayRunnable != null) {
            autoPlayHandler.removeCallbacks(autoPlayRunnable);
        }

        android.util.Log.d("AutoPlay", "⏸️ 停止自动轮播");
    }
    private void updateLikeUI(boolean isLiked){
        if(isLiked){
            ivLike.setImageResource(R.drawable.ic_like_detail_fill);
        }else{
            ivLike.setImageResource(R.drawable.ic_like_detail);
        }
    }
    private void updateFollowUI(boolean isFollowed){
        btnFollow.setSelected(isFollowed);
        btnFollow.setText(isFollowed ? "已关注" : "关注");
    }
    private void showShareDialog() {
        ShareDialog dialog = new ShareDialog(this);
        dialog.show();
    }
    // ✅ 生命周期管理
    @Override
    protected void onPause() {
        super.onPause();
        android.util.Log.d("MediaPlayer", "⏸️ onPause - 暂停播放");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.pause();
            } catch (Exception e) {
                android.util.Log.e("MediaPlayer", "暂停失败", e);
            }
        }
        // 停止轮播
        stopAutoPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("MediaPlayer", "▶️ onResume");
        // ✅ 修复：只有在已暂停且未静音时才恢复播放
        if (hasMusic && mediaPlayer != null) {
            try {
                boolean isMuted = musicStateManager.isMuted();
                // 检查状态：只在 Paused 状态才能 start()
                if (!mediaPlayer.isPlaying() && !isMuted) {
                    mediaPlayer.start();
                    android.util.Log.d("MediaPlayer", "恢复播放");
                }
            } catch (IllegalStateException e) {
                android.util.Log.e("MediaPlayer", "恢复播放失败: " + e.getMessage());
                // 状态错误时重新初始化
                if (post != null && post.getMusic() != null) {
                    initAndPlayMusic(post.getMusic());
                }
            }
        }
        // 恢复轮播
        if (!musicStateManager.isMuted() && totalImageCount > 1) {
            startAutoPlay();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理轮播
        stopAutoPlay();
        if (autoPlayHandler != null) {
            autoPlayHandler.removeCallbacksAndMessages(null);
        }
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mediaPlayer = null;
            }
        }
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