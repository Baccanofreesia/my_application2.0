package com.example.myapplication.adapter;

import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.myapplication.model.Clip;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import java.util.List;

import com.bumptech.glide.load.DataSource;


public class ClipPagerAdapter extends RecyclerView.Adapter<ClipPagerAdapter.ClipViewHolder> {

    private Context context;
    private List<Clip> clips;
    private int containerHeight;

    public interface OnVideoPlayListener {
        void onVideoStart(int position);
        void onVideoComplete(int position);
    }

    private OnVideoPlayListener videoListener;

    public void setOnVideoPlayListener(OnVideoPlayListener listener) {
        this.videoListener = listener;
    }

    public ClipPagerAdapter(Context context, List<Clip> clips, int containerHeight) {
        this.context = context;
        this.clips = clips;
        this.containerHeight = containerHeight;
    }

    @NonNull
    @Override
    public ClipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout root = new FrameLayout(context);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                containerHeight
        ));
        return new ClipViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ClipViewHolder holder, int position) {
        Clip clip = clips.get(position);
        holder.container.removeAllViews();
        if (clip.getType() == 0) {
            // ======================
            //      图片
            // ======================
//            ImageView iv = new ImageView(context);
//            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
//
//            Glide.with(context)
//                    .load(clip.getUrl())
//                    .into(iv);
//
//            holder.container.removeAllViews();
//            holder.container.addView(iv);
            showImage(holder, clip);
        } else {
            // ======================
            //      视频
            // ======================
//            PlayerView playerView = new PlayerView(context);
//            SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();
//
//            holder.player = player;
//            holder.playerView = playerView;
//
//            holder.container.removeAllViews();
//            holder.container.addView(playerView);
//
//            MediaItem item = MediaItem.fromUri(clip.getUrl());
//            player.setMediaItem(item);
//            player.prepare();
//
//            // ❗自动播放
//            player.setPlayWhenReady(true);
//
//            // ❗独立循环播放
//            player.setRepeatMode(Player.REPEAT_MODE_ONE);
//
//            // ❗通知 Activity 播放开始
//            if (videoListener != null) {
//                videoListener.onVideoStart(position);
//            }
//
//            // ❗监听播放结束（用于自动轮播）
//            player.addListener(new Player.Listener() {
//                @Override
//                public void onPlaybackStateChanged(int state) {
//                    if (state == Player.STATE_ENDED) {
//                        if (videoListener != null) {
//                            videoListener.onVideoComplete(position);
//                        }
//                    }
//                }
//            });
        }
    }
    private void showImage(ClipViewHolder holder, Clip clip) {

        ProgressBar progress = new ProgressBar(context);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
                80,80
        );
        p.gravity = Gravity.CENTER;
        holder.container.addView(progress, p);

        ImageView iv = new ImageView(context);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(context)
                .load(clip.getUrl())
                .placeholder(null)
                .error(android.R.drawable.stat_notify_error)
                .into(iv);

        // 加载成功 / 失败回调
        Glide.with(context)
                .load(clip.getUrl())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource) {
                        holder.container.removeView(progress);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.container.removeView(progress);
                        return false;
                    }
                })
                .into(iv);

        holder.container.addView(iv);
    }

    private void showVideo(ClipViewHolder holder, Clip clip, int position) {

        PlayerView playerView = new PlayerView(context);
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();

        holder.player = player;
        holder.playerView = playerView;

        holder.container.addView(playerView);

        playerView.setUseController(false);
        MediaItem item = MediaItem.fromUri(clip.getUrl());
        player.setMediaItem(item);
        player.prepare();
        player.setPlayWhenReady(true);

        // 播放开始
        if (videoListener != null) videoListener.onVideoStart(position);

        // 播放完 → 下一个
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    if (videoListener != null) {
                        videoListener.onVideoComplete(position);
                    }
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
            holder.player.release();
        }
    }

    static class ClipViewHolder extends RecyclerView.ViewHolder {
        FrameLayout container;
        SimpleExoPlayer player;
        PlayerView playerView;

        public ClipViewHolder(@NonNull View itemView) {
            super(itemView);
            container = (FrameLayout) itemView;
        }
    }
}

