package com.example.myapplication.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.text.SpannableString;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplication.R;
import com.example.myapplication.adapter.ClipPagerAdapter;
import com.example.myapplication.dialog.ShareDialog;
import com.example.myapplication.model.Author;
import com.example.myapplication.model.Clip;
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
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.view.GestureDetector;

/**
 * 作品详情页
 */
public class PostDetailActivity extends AppCompatActivity {
    // 滑动相关
    private float startX, startY;
    private boolean isSwipeGestureActive = false;
    // 在类成员里加上
    private GestureDetector gestureDetector;
    private boolean isLongPressSwipeMode = false;   // 长按激活的标志
    private static final float SWIPE_THRESHOLD = 120; // 触发退出的距离（dp）
    private static final float EDGE_THRESHOLD = 60;   // 边缘识别区域（dp）
    private boolean isSwipeEnabled = true;           // 控制是否启用侧滑

    // 动画相关
    private View rootView;
    private View dimOverlay; // 蒙层
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
    private FrameLayout volumeContainer;
    //内容区
    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvDate;
    //底部交互区
    private TextView etComment;
    private ConstraintLayout layoutLike;
    private ConstraintLayout layoutComment;
    private ConstraintLayout layoutCollect;
    private ConstraintLayout layoutShare;
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
    private boolean isMediaPrepared = false;
    private MediaPlayer mediaPlayer;
    // 自动轮播相关
    private Handler autoPlayHandler= new Handler(Looper.getMainLooper());;
    private Runnable autoPlayRunnable;
    private static final long AUTO_PLAY_INTERVAL = 10000;
    private boolean isAutoPlaying = false;  // 是否正在自动轮播
    private boolean isUserScrolling = false;  // 用户是否在手动滑动
    private int totalClipCount = 0;  // 图片总数
    // 图片加载状态管理
    private ClipPagerAdapter adapter;
    private boolean isCurrentClipLoaded = false;  // 当前图片是否加载完成
    private int maxLoadWaitTime = 6000;  // 最大等待时间
    private final List<SimpleExoPlayer> preloadPlayers = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //转场
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
// 配置转场动画
        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(300);
        getWindow().setSharedElementEnterTransition(changeBounds);
        getWindow().setSharedElementReturnTransition(changeBounds);

        // 背景淡入淡出
        Fade fade = new Fade();
        fade.setDuration(250);
        getWindow().setEnterTransition(fade);
        getWindow().setReturnTransition(fade);
        setContentView(R.layout.activity_work_detail);
        setupBackPressHandler();
        // 初始化侧滑
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                float x = e.getRawX();
                if (x < dpToPx(60) && isSwipeEnabled && !isSwipeGestureActive) {
                    isSwipeGestureActive = true;
                    isLongPressSwipeMode = true;
                    startX = x;
                    startY = e.getRawY();
                    dimOverlay.setVisibility(View.VISIBLE);
                    viewPagerImages.setUserInputEnabled(false);   // 禁用 ViewPager2 滑动
                    android.util.Log.d("SwipeBack", "长按左边缘激活侧滑");
                    getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            }
        });
        setupSwipeBack();
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
    private void setupSwipeBack() {
        rootView = findViewById(R.id.root_container);

        // 创建蒙层
        dimOverlay = new View(this);
        dimOverlay.setBackgroundColor(0x80000000);
        dimOverlay.setVisibility(View.GONE);

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        decorView.addView(dimOverlay, 0, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        // 如果侧滑手势已激活，优先处理
        if (isSwipeGestureActive) {
            handleSwipeTouch(event);
            return true;
        }


        // 默认处理
        return super.dispatchTouchEvent(event);
    }

    /**
     * 处理滑动手势
     */
    private void handleSwipeTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float currentX = event.getRawX();
                float deltaX = currentX - startX;

                // 只允许向右滑动
                if (deltaX > 0) {
                    // 计算进度（0 ~ 1）
                    float progress = Math.min(deltaX / getScreenWidth(), 1f);

                    float scaleFactor = progress < 0.5f ? 0.3f : 0.15f;
                    float scale = 1f - (scaleFactor * progress);

                    // 应用变换
                    rootView.setTranslationX(deltaX);
                    rootView.setScaleX(scale);
                    rootView.setScaleY(scale);

                    // 蒙层透明度
                    float alpha = 1f - progress;
                    dimOverlay.setAlpha(alpha);

                    rootView.setTranslationZ(16f * progress); // 添加阴影深度

                    android.util.Log.d("SwipeBack", "滑动: Δx=" + (int)deltaX +
                            ", progress=" + String.format("%.2f", progress) +
                            ", scale=" + String.format("%.2f", scale));
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isSwipeGestureActive) {
                    float totalDeltaX = event.getRawX() - startX;

                    android.util.Log.d("SwipeBack", "松手: totalDeltaX=" + totalDeltaX + ", threshold=" + dpToPx(SWIPE_THRESHOLD));

                    // 判断是否触发退出
                    if (totalDeltaX > dpToPx(SWIPE_THRESHOLD)) {
                        // 完成退出动画
                        animateFinish();
                    } else {
                        // 回弹
                        animateBack();
                    }
                    isSwipeGestureActive = false;
                    isLongPressSwipeMode = false;
                    viewPagerImages.setUserInputEnabled(true);
                }
                break;
        }
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
        volumeContainer = findViewById(R.id.volume_container);
        layoutLike = findViewById(R.id.layout_like);
        layoutCollect = findViewById(R.id.layout_collect);
        layoutComment = findViewById(R.id.layout_comment);
        layoutShare = findViewById(R.id.layout_share);
        ivLike = findViewById(R.id.iv_like);

        tvLikeCount = findViewById(R.id.tv_like_count);
        tvCommentCount = findViewById(R.id.tv_comment_count);
        tvCollectCount = findViewById(R.id.tv_collect_count);
        tvShareCount = findViewById(R.id.tv_share_count);
    }
    private void loadIntentData(){
        String postId = getIntent().getStringExtra("post_id");
        //空值检查
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "作品ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        post = DataManager.getInstance().getPostById(postId);
        if (post == null) {
            // 显示加载失败
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

        // 标题
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
        preloadInitialClips();
        // 检查是否有音频
        Music music = post.getMusic();
        hasMusic = (music != null && music.getUrl() != null && !music.getUrl().isEmpty());
        if (totalClipCount > 1||hasMusic) {
            volumeContainer.setVisibility(View.VISIBLE);
            if (hasMusic) {
                initAndPlayMusic(music);
            }
            // 更新UI
            updateVolumeUI();

            // 如果未静音,开始自动轮播
            if (!musicStateManager.isMuted()) {
                startAutoPlay();
            }

        } else {
            volumeContainer.setVisibility(View.GONE);
            hasMusic = false;
        }

        // 点赞数
        int baseLike = LikeCountGenerator.generateLikeCount(post.getPostId());
        boolean liked = likeManager.isActive(post.getPostId());
        tvLikeCount.setText(String.valueOf(liked ? baseLike + 1 : baseLike));
        updateLikeUI(liked);
        boolean  followed = followManager.isActive(post.getPostId());
        updateFollowUI(followed);

        // 评论 收藏伪随机
        tvCommentCount.setText(String.valueOf(CommentCountGenerator.generate(post.getPostId())));
        tvCollectCount.setText(String.valueOf(CollectCountGenerator.generate(post.getPostId())));
        tvShareCount.setText(String.valueOf(ShareCountGenerator.generate(post.getPostId())));
    }
    private void preloadInitialClips() {
        if (post.getClips() == null || post.getClips().isEmpty()) return;

        List<Clip> clips = post.getClips();
        int preloadCount = Math.min(3, clips.size());

        for (int i = 0; i < preloadCount; i++) {
            Clip clip = clips.get(i);
            if (clip.getType() == 0) {
                // 图片用 Glide 预加载
                Glide.with(this)
                        .load(clip.getUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload();
            } else {
                // 视频用 ExoPlayer
                SimpleExoPlayer preloadPlayer = new SimpleExoPlayer.Builder(this).build();
                preloadPlayer.setMediaItem(MediaItem.fromUri(clip.getUrl()));
                preloadPlayer.prepare();
                preloadPlayer.setPlayWhenReady(false);  // 不播放，只缓存
                // 监听缓存进度
                preloadPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (playbackState == Player.STATE_READY) {
                            long bufferedPosition = preloadPlayer.getBufferedPosition();
                            long duration = preloadPlayer.getDuration();
                            if (duration > 0) {
                                float percent = bufferedPosition * 100f / duration;
                                android.util.Log.d("VideoPreload",
                                        String.format("视频预缓存完成 %.1f%%: %s", percent, clip.getUrl()));
                            }
                        }
                    }

                    @Override
                    public void onPositionDiscontinuity(
                            Player.PositionInfo oldPosition,
                            Player.PositionInfo newPosition,
                            int reason) {
                        long buffered = preloadPlayer.getBufferedPosition();
                        long duration = preloadPlayer.getDuration();
                        if (duration > 0) {
                            float percent = buffered * 100f / duration;
                            android.util.Log.v("VideoPreload",
                                    String.format("缓存进度: %.1f%%", percent));
                        }
                    }
                });
                preloadPlayers.add(preloadPlayer);
                android.util.Log.d("Preload", "开始预加载视频: " + i + " " + clip.getUrl());
            }

        }

    }
    /**
     * ✅ 预加载指定位置的图片
     */
    private void preloadNextClip(int position) {
        if (position < 0 || position >= totalClipCount) return;

        Clip clip = post.getClips().get(position);
        if (clip.getType() == 0) {
            // 图片
            Glide.with(this)
                    .load(clip.getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload();
        } else {
            // 视频：创建一个临时的 Player 进行预缓存
            SimpleExoPlayer tempPlayer = new SimpleExoPlayer.Builder(this).build();
            tempPlayer.setMediaItem(MediaItem.fromUri(clip.getUrl()));
            tempPlayer.prepare();
            tempPlayer.setPlayWhenReady(false);
            tempPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        android.util.Log.d("PreloadNext", "下一条视频预缓存完成: " + position);
                        tempPlayer.release();
                    }
                }
            });

        }

        android.util.Log.d("PreloadNext", "预加载下一条: " + position + " type=" + clip.getType());
    }
    private void initAndPlayMusic(Music music) {
        // 先释放旧的 MediaPlayer
        releaseMediaPlayer();
        try {
            android.util.Log.d("MediaPlayer", "📀 初始化音频: " + music.getUrl());
            // 1. 创建 MediaPlayer
            mediaPlayer = new MediaPlayer();
            // 1. 设置音频流类型
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build());
            // 2. 设置音频源
            if (music.getUrl().startsWith("file:///android_asset/")) {
                String assetPath = music.getUrl().substring("file:///android_asset/".length());
                AssetFileDescriptor afd = getAssets().openFd(assetPath);
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                android.util.Log.d("MediaPlayer", "成功打开 assets 音频: " + assetPath);
            } else {
                mediaPlayer.setDataSource(music.getUrl());
            }
            // 根据全局状态设置初始音量
            boolean isMuted = musicStateManager.isMuted();
            if (isMuted) {
                mediaPlayer.setVolume(0f, 0f);
                android.util.Log.d("MediaPlayer", "初始化为静音");
            } else {
                mediaPlayer.setVolume(1f, 1f);
                android.util.Log.d("MediaPlayer", "初始化为有声");
            }
            // 4. 设置循环播放
            mediaPlayer.setLooping(true);
            // 5.重置准备标志
            isMediaPrepared = false;

            // 6. 准备完成后自动播放
            mediaPlayer.setOnPreparedListener(mp -> {
                isMediaPrepared = true;
                if (!isFinishing() && !isDestroyed()) {
                    if (!isMuted) {
                        mp.start();
                    }
                    android.util.Log.d("MediaPlayer", "开始播放");
                }
            });

            // 7. 错误处理
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                isMediaPrepared = false;
                android.util.Log.e("MediaPlayer", "播放失败: " + what + ", " + extra);
                Toast.makeText(this, "音频播放失败", Toast.LENGTH_SHORT).show();
                return true;
            });
            // 5. 异步准备
            mediaPlayer.prepareAsync();
            android.util.Log.d("MediaPlayer", "开始异步准备...");
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

            // 找到话题词结束位置
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

                    ds.setColor(Color.parseColor("#04498D")); // 普蓝色
                    ds.setUnderlineText(false); // 去除下划线
                }
            };

            spannableString.setSpan(clickableSpan, start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = end;
        }

        tvContent.setText(spannableString);
        tvContent.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        tvContent.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }
    private void openHashtagPage(String hashtagText) {
        Intent intent = new Intent(this, HashtagActivity.class);
        intent.putExtra("hashtag", hashtagText);
        startActivity(intent);
        // 设置横滑动画
        overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation);
    }

    private void setupClips() {
        List<Clip> clips = post.getClips();
        if (clips == null || clips.isEmpty()) return;
        totalClipCount = clips.size();

        // 动态计算首片段比例并设置 ViewPager 高度
        adjustViewPagerHeight(clips.get(0));
        adapter = new ClipPagerAdapter(this, clips);
        // 设置片段加载监听
        adapter.setOnClipLoadListener((position, success) -> {
            android.util.Log.d("ClipLoad", "片段 " + position + (success ? " 加载成功" : " 加载失败"));

            // 只有当前显示的片段加载完成才标记
            if (position == viewPagerImages.getCurrentItem()) {
                isCurrentClipLoaded = success;
                android.util.Log.d("AutoPlay", "当前片段 " + position + " 加载状态: " + (success ? "成功" : "失败"));
            }
        });
        // 设置视频播放监听
        adapter.setOnVideoPlayListener(new ClipPagerAdapter.OnVideoPlayListener() {
            @Override
            public void onVideoStart(int position) {
                // 视频开始：暂停背景音乐
                pauseMusicIfNeeded();
            }

            @Override
            public void onVideoComplete(int position) {
                android.util.Log.d("VideoCallback", "视频播放完成: " + position);

                boolean isMuted = musicStateManager.isMuted();
                android.util.Log.d("VideoCallback", "当前状态: isMuted=" + isMuted +
                        ", currentItem=" + viewPagerImages.getCurrentItem());
                if (!isMuted && position == viewPagerImages.getCurrentItem()) {
                    int nextPosition = (position + 1) % totalClipCount;
                    android.util.Log.d("VideoCallback", "🔄 自动切换到: " + nextPosition);

                    viewPagerImages.postDelayed(() -> {
                        viewPagerImages.setCurrentItem(nextPosition, true);
                    }, 300); // 延迟切换，避免卡顿
                }
            }
        });
        viewPagerImages.setAdapter(adapter);
        // 设置进度条
        if (clips.size() > 1) {
            setupIndicators(clips.size());
            layoutIndicator.setVisibility(View.VISIBLE);
        } else {
            layoutIndicator.setVisibility(View.GONE);
        }
        // 第一片段默认已加载
        isCurrentClipLoaded = true;
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                preloadNextClip((position + 1) % totalClipCount);

                viewPagerImages.postDelayed(() -> {
                    adjustMediaForCurrentClip();
                }, 150); // 150ms 足够视图创建完成

                // 重启图片轮播计时器
                if (!musicStateManager.isMuted()) {
                    Clip clip = post.getClips().get(position);
                    if (clip.getType() == 0) {
                        stopAutoPlay();
                        startAutoPlay();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    isUserScrolling = true;
                    stopAutoPlay();
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    isUserScrolling = false;
                    if (!musicStateManager.isMuted()) {
                        // 延迟启动，避免和 onPageSelected 冲突
                        viewPagerImages.postDelayed(() -> {
                            if (!isAutoPlaying) {
                                startAutoPlay();
                            }
                        }, 200);
                    }
                }
            }
        });
    }
    private void adjustViewPagerHeight(Clip firstClip) {
        // 如果图片列表为空，使用默认高度
        if (post.getClips() == null || post.getClips().isEmpty()) {
            return;
        }
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
            if (i == 0) {
                indicator.setBackgroundResource(R.drawable.bg_indicator_active);
            } else {
                indicator.setBackgroundResource(R.drawable.bg_indicator_item);
            }
            layoutIndicator.addView(indicator);
        }
    }
    /**
     * 更新进度条状态
     */
    private void updateIndicators(int position) {
        for (int i = 0; i < layoutIndicator.getChildCount(); i++) {
            View indicator = layoutIndicator.getChildAt(i);
            if (i == position) {
                indicator.setBackgroundResource(R.drawable.bg_indicator_active);
            } else {
                indicator.setBackgroundResource(R.drawable.bg_indicator_item);
            }
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
        btnBack.setOnClickListener(v ->  finishAfterTransition());
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
            // TODO: 收藏状态管理
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
    private void toggleVolume() {
        // 切换全局状态
        boolean newMutedState = musicStateManager.toggleMuted();
        // 获取当前片段类型
        int position = viewPagerImages.getCurrentItem();
        Clip currentClip = post.getClips().get(position);

        // 1. 根据片段类型更新媒体状态
        if (currentClip.getType() == 1) {
            SimpleExoPlayer player = getCurrentPlayer();
            if (player != null) {
                player.setVolume(newMutedState ? 0f : 1f);
                player.setRepeatMode(newMutedState ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
                android.util.Log.d("VolumeToggle", "视频页切换音量，保持当前位置: " + player.getCurrentPosition());
            }
        } else {
            if (hasMusic && mediaPlayer != null) {
                try {
                    mediaPlayer.setVolume(newMutedState ? 0f : 1f, newMutedState ? 0f : 1f);
                    if (!newMutedState && isMediaPrepared && !mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        android.util.Log.d("VolumeToggle", "图片页开启BGM");
                    }
                } catch (Exception e) {
                    android.util.Log.e("VolumeToggle", "BGM控制失败", e);
                }
            }
        }

        // 2. 控制轮播(多图时)
        if (totalClipCount > 1) {
            if (newMutedState) {
                // 静音 = 停止轮播
                stopAutoPlay();
            } else {
                // 开启声音 = 开始轮播
                startAutoPlay();
            }
        }

        // 更新 UI
        updateVolumeUI();
    }

    private void adjustMediaForCurrentClip() {
        int position = viewPagerImages.getCurrentItem();
        Clip currentClip = post.getClips().get(position);
        boolean isMuted = musicStateManager.isMuted();

        if (currentClip.getType() == 1) { // 视频
            // 更新视频player的volume和repeat
            SimpleExoPlayer player = getCurrentPlayer();
            if (player != null) {
                player.setVolume(isMuted ? 0f : 1f);
                player.setRepeatMode(isMuted ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
                int playbackState = player.getPlaybackState();
                if (playbackState == Player.STATE_IDLE) {
                    player.prepare();
                    player.seekTo(0);
                    player.setPlayWhenReady(true);
                    android.util.Log.d("VideoFix", "播放器未初始化，prepare 后从头播放");

                } else if (playbackState == Player.STATE_ENDED) {
                    player.seekTo(0);
                    player.setPlayWhenReady(true);
                    android.util.Log.d("VideoFix", "播放已结束，重置到开头");

                } else {
                    // 播放器正常状态
                    player.seekTo(0);
                    player.setPlayWhenReady(true);
                    android.util.Log.d("VideoFix", "正常状态，从头播放");
                }
            }
            // 暂停 BGM
            if (hasMusic && mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                android.util.Log.d("MediaControl", "暂停BGM");
            }
        } else { // 图片
            if (hasMusic && mediaPlayer != null) {
                try {
                    mediaPlayer.setVolume(isMuted ? 0f : 1f, isMuted ? 0f : 1f);
                    if (!isMuted && isMediaPrepared && !mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        android.util.Log.d("MediaControl", "图片页恢复 BGM");
                    }
                }catch (Exception e) {
                    android.util.Log.e("MediaControl", "BGM 恢复失败，重新初始化", e);
                    if (post.getMusic() != null) initAndPlayMusic(post.getMusic());
                }
            }
        }
    }
    private SimpleExoPlayer getCurrentPlayer() {
        try {
            RecyclerView recyclerView = (RecyclerView) viewPagerImages.getChildAt(0);
            if (recyclerView == null) return null;

            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(viewPagerImages.getCurrentItem());
            if (holder instanceof ClipPagerAdapter.ClipViewHolder) {
                return ((ClipPagerAdapter.ClipViewHolder) holder).player;
            }
            return null;
        } catch (Exception e) {
            android.util.Log.e("GetPlayer", "获取播放器失败", e);
            return null;
        }
    }
    private void pauseMusicIfNeeded() {
        if (hasMusic && mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
    /**
     * 开始自动轮播
     */
    private void startAutoPlay() {
        if (totalClipCount <= 1 || musicStateManager.isMuted()) {
            android.util.Log.d("AutoPlay", "不满足轮播条件");
            return;
        }
        if (isAutoPlaying) {
            android.util.Log.d("AutoPlay", "轮播已在运行");
            return;
        }
        int position = viewPagerImages.getCurrentItem();
        Clip currentClip = post.getClips().get(position);
        if (currentClip.getType() == 1) {
            isAutoPlaying = true; // 标记为轮播中
            return;
        }
        isAutoPlaying = true;
        android.util.Log.d("AutoPlay", "开始自动轮播");

        autoPlayRunnable = new Runnable() {
            private long startWaitTime = 0;  // 记录开始等待的时间
            @Override
            public void run() {
                if (musicStateManager.isMuted() || isUserScrolling || totalClipCount <= 1)  {
                    isAutoPlaying = false;
                    android.util.Log.d("AutoPlay", "检测到停止条件");
                    return;
                }
                if (!isCurrentClipLoaded) {
                    // 初始化等待时间
                    if (startWaitTime == 0) {
                        startWaitTime = System.currentTimeMillis();
                        android.util.Log.d("AutoPlay", "开始等待图片加载...");
                    }

                    long waitedTime = System.currentTimeMillis() - startWaitTime;

                    // 如果等待时间超过最大限制，强制切换
                    if (waitedTime > maxLoadWaitTime) {
                        android.util.Log.w("AutoPlay", "图片加载超时（" + waitedTime + "ms），强制切换");
                        isCurrentClipLoaded = true;  // 强制标记为已加载
                        startWaitTime = 0;
                    } else {
                        // 继续等待，500ms 后再检查
                        android.util.Log.d("AutoPlay", "等待中... 已等待 " + waitedTime + "ms");
                        autoPlayHandler.postDelayed(this, 500);
                        return;
                    }
                }

                int currentPosition = viewPagerImages.getCurrentItem();
                int nextPosition = (currentPosition + 1) % totalClipCount;
                android.util.Log.d("AutoPlay", "切换: " + currentPosition + " → " + nextPosition);

                // 预加载下一张
                int preloadPosition = (nextPosition + 1) % totalClipCount;
                preloadNextClip(preloadPosition);

                // 切换前重置状态
                isCurrentClipLoaded = false;
                startWaitTime = 0;
                viewPagerImages.setCurrentItem(nextPosition, true);
                Clip nextClip = post.getClips().get(nextPosition);
                if (nextClip.getType() == 1) {
                    android.util.Log.d("AutoPlay", "下一个是视频，暂停定时器");
                } else {
                    autoPlayHandler.postDelayed(this, AUTO_PLAY_INTERVAL);
                }
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

        android.util.Log.d("AutoPlay", "停止自动轮播");
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
        android.util.Log.d("MediaPlayer", "onPause - 暂停播放");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.pause();
            } catch (Exception e) {
                android.util.Log.e("MediaPlayer", "暂停失败", e);
            }
        }
        SimpleExoPlayer player = getCurrentPlayer();
        if (player != null && player.isPlaying()) {
            player.setPlayWhenReady(false);  // 暂停
            android.util.Log.d("VideoPlayer", "已暂停视频，当前位置: " + player.getCurrentPosition());
        }
        // 停止轮播
        stopAutoPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("MediaPlayer", "▶️ onResume");

        // 恢复轮播
        if (!musicStateManager.isMuted() && totalClipCount > 1) {
            startAutoPlay();
        }
        // 恢复当前媒体播放
        int position = viewPagerImages.getCurrentItem();
        Clip currentClip = post.getClips().get(position);
        boolean isMuted = musicStateManager.isMuted();

        if (currentClip.getType() == 1) {
            // 视频：从暂停位置继续播放
            SimpleExoPlayer player = getCurrentPlayer();
            if (player != null) {
                player.setVolume(isMuted ? 0f : 1f);
                player.setRepeatMode(isMuted ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
                player.setPlayWhenReady(true);  // 继续播放
                android.util.Log.d("VideoPlayer", "从暂停位置恢复视频播放");
            }
        } else {
            // 图片：恢复 BGM
            if (hasMusic && mediaPlayer != null && !isMuted && isMediaPrepared) {
                try {
                    mediaPlayer.start();
                    android.util.Log.d("MediaPlayer", "恢复 BGM 播放");
                } catch (Exception e) {
                    android.util.Log.e("MediaPlayer", "恢复失败", e);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (SimpleExoPlayer player : preloadPlayers) {
            if (player != null) {
                try {
                    player.release();
                } catch (Exception e) {
                    android.util.Log.e("Lifecycle", "释放预加载播放器失败", e);
                }
            }
        }
        preloadPlayers.clear();
        // 清理轮播
        stopAutoPlay();
        if (autoPlayHandler != null) {
            autoPlayHandler.removeCallbacksAndMessages(null);
        }
        releaseMediaPlayer();
        if (dimOverlay != null && dimOverlay.getParent() != null) {
            ((ViewGroup) dimOverlay.getParent()).removeView(dimOverlay);
        }
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
                isMediaPrepared = false;
            }
        }
    }
    private void animateFinish() {
        android.util.Log.d("SwipeBack", "执行退出动画");

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(250);

        float startTranslationX = rootView.getTranslationX();
        float startScale = rootView.getScaleX();
        float startAlpha = dimOverlay.getAlpha();

        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();

            // 继续滑动到屏幕外
            rootView.setTranslationX(startTranslationX + (getScreenWidth() - startTranslationX) * progress);

            // 继续缩小
            float scale = startScale - (startScale - 0.7f) * progress;
            rootView.setScaleX(scale);
            rootView.setScaleY(scale);

            // 蒙层完全消失
            dimOverlay.setAlpha(startAlpha * (1 - progress));
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finishAfterTransition();
            }
        });

        animator.start();
    }

    /**
     * 回弹动画
     */
    private void animateBack() {
        android.util.Log.d("SwipeBack", "↩️ 执行回弹动画");

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(250);

        float startTranslationX = rootView.getTranslationX();
        float startScale = rootView.getScaleX();
        float startAlpha = dimOverlay.getAlpha();

        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();

            // 回到原位
            rootView.setTranslationX(startTranslationX * (1 - progress));

            // 恢复大小
            float scale = startScale + (1f - startScale) * progress;
            rootView.setScaleX(scale);
            rootView.setScaleY(scale);

            // 蒙层恢复
            dimOverlay.setAlpha(startAlpha + (1f - startAlpha) * progress);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dimOverlay.setVisibility(View.GONE);
                rootView.setTranslationZ(0); // 恢复阴影
            }
        });

        animator.start();
    }


    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 执行共享元素返回动画
                finishAfterTransition();
            }
        });
    }

}