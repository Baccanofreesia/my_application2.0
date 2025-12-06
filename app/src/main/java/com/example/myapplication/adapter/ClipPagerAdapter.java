package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.exoplayer2.PlaybackException;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.myapplication.model.Clip;
import com.example.myapplication.utils.MusicStateManager;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;

public class ClipPagerAdapter extends RecyclerView.Adapter<ClipPagerAdapter.ClipViewHolder> {

    private Context context;
    private List<Clip> clips;

    public interface OnVideoPlayListener {
        void onVideoStart(int position);
        void onVideoComplete(int position);
    }

    public interface OnClipLoadListener {
        void onClipLoaded(int position, boolean success);
    }

    private OnVideoPlayListener videoListener;
    private OnClipLoadListener clipLoadListener;

    public void setOnVideoPlayListener(OnVideoPlayListener listener) {
        this.videoListener = listener;
    }

    public void setOnClipLoadListener(OnClipLoadListener listener) {
        this.clipLoadListener = listener;
    }

    public ClipPagerAdapter(Context context, List<Clip> clips) {
        this.context = context;
        this.clips = clips;

    }

    @NonNull
    @Override
    public ClipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout root = new FrameLayout(context);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return new ClipViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ClipViewHolder holder, int position) {
        Clip clip = clips.get(position);
        holder.container.removeAllViews();
        if (clip.getType() == 0) {
            showImage(holder, clip, position);
        } else {
            showVideo(holder, clip, position);
        }
    }

    private void showImage(ClipViewHolder holder, Clip clip, int position) {
        ProgressBar progress = new ProgressBar(context);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(80, 80);
        p.gravity = Gravity.CENTER;
        holder.container.addView(progress, p);

        ImageView iv = new ImageView(context);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(context)
                .load(clip.getUrl())
                .placeholder(null)
                .error(android.R.drawable.stat_notify_error)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.container.removeView(progress);
                        if (clipLoadListener != null) {
                            clipLoadListener.onClipLoaded(position, false);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.container.removeView(progress);
                        if (clipLoadListener != null) {
                            clipLoadListener.onClipLoaded(position, true);
                        }
                        return false;
                    }
                })
                .into(iv);

        holder.container.addView(iv);
    }

    private void showVideo(ClipViewHolder holder, Clip clip, int position) {
        // 加载指示器
        ProgressBar progress = new ProgressBar(context);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(80, 80);
        p.gravity = Gravity.CENTER;
        holder.container.addView(progress, p);

        // 错误指示器
        ImageView errorIv = new ImageView(context);
        errorIv.setVisibility(View.GONE);
        errorIv.setImageResource(android.R.drawable.stat_notify_error);
        FrameLayout.LayoutParams ep = new FrameLayout.LayoutParams(80, 80);
        ep.gravity = Gravity.CENTER;
        holder.container.addView(errorIv, ep);

        // 创建 PlayerView
        PlayerView playerView = new PlayerView(context);
        playerView.setUseController(false);
        playerView.setBackgroundColor(Color.BLACK);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        //立即添加到容器
        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        holder.container.addView(playerView, videoParams);

        //创建 Player
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();
        holder.player = player;
        holder.playerView = playerView;

        playerView.setPlayer(player);

        // 根据全局状态初始化
        boolean isMuted = MusicStateManager.getInstance().isMuted();
        player.setVolume(isMuted ? 0f : 1f);
        player.setRepeatMode(isMuted ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);

        android.util.Log.d("VideoInit", "初始化视频 " + position +
                ": volume=" + (isMuted ? "0" : "1") +
                ", repeat=" + (isMuted ? "ONE" : "OFF"));

        // 设置媒体并播放
        MediaItem item = MediaItem.fromUri(clip.getUrl());
        player.setMediaItem(item);
        player.prepare();
        player.setPlayWhenReady(true);

        // 监听器
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progress.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    progress.setVisibility(View.GONE);
                    errorIv.setVisibility(View.GONE);

                    android.util.Log.d("VideoReady", "视频准备完成: " + position);

                    if (videoListener != null) {
                        videoListener.onVideoStart(position);
                    }
                    if (clipLoadListener != null) {
                        clipLoadListener.onClipLoaded(position, true);
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    android.util.Log.d("VideoEnded", "视频播完: " + position);

                    if (videoListener != null) {
                        videoListener.onVideoComplete(position);
                    }
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                progress.setVisibility(View.GONE);
                errorIv.setVisibility(View.VISIBLE);
                android.util.Log.e("VideoError", "播放失败: " + error.getMessage());

                if (clipLoadListener != null) {
                    clipLoadListener.onClipLoaded(position, false);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return clips.size();
    }

    @Override
    public void onViewRecycled(@NonNull ClipViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.player != null) {
            try {
                if (holder.playerView != null) {
                    holder.playerView.setPlayer(null);
                }
                holder.player.stop();
                holder.player.release();

                android.util.Log.d("VideoRecycle", "释放视频播放器");
            } catch (Exception e) {
                android.util.Log.e("VideoRecycle", "释放失败", e);
            } finally {
                holder.player = null;
                holder.playerView = null;
            }
        }
    }

    public static class ClipViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout container;
        public SimpleExoPlayer player;
        public PlayerView playerView;

        public ClipViewHolder(@NonNull View itemView) {
            super(itemView);
            container = (FrameLayout) itemView;
        }
    }
}