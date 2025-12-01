package com.example.myapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplication.activity.PostDetailActivity;
import com.example.myapplication.utils.AnimationUtils;
import com.example.myapplication.utils.DataManager;
import com.example.myapplication.utils.LikeCountGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.myapplication.R;
import com.example.myapplication.model.Author;
import com.example.myapplication.model.Clip;
import com.example.myapplication.model.Post;
import com.example.myapplication.utils.LikeManager;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> posts;
    private LikeManager likeManager;
    public PostAdapter(Context context,List<Post> posts){
        this.context=context;
        this.posts=posts;
        this.likeManager=new LikeManager(context);
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_work, parent, false);
        return new PostViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder,int position){
        Post post=posts.get(position);
        holder.bind(post,position);
    }
    @Override
    public int getItemCount(){
        return posts.size();
    }
    public void addData(List<Post> newPosts){
        int startPosition=posts.size();
        posts.addAll(newPosts);
        notifyItemRangeInserted(startPosition,newPosts.size());
    }
    public void refreshData(List<Post> newPosts){
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }
    class PostViewHolder extends RecyclerView.ViewHolder{
        ImageView ivCover;
        TextView tvTitle;
        ImageView ivAvatar;
        TextView tvNickname;
        ImageView ivLike;
        TextView tvLikeCount;
        View layoutLike;
        PostViewHolder(View itemView){
            super(itemView);
            ivCover=itemView.findViewById(R.id.iv_cover);
            tvTitle=itemView.findViewById(R.id.tv_title);
            ivAvatar=itemView.findViewById(R.id.iv_avatar);
            tvNickname=itemView.findViewById(R.id.tv_nickname);
            ivLike=itemView.findViewById(R.id.iv_like);
            tvLikeCount=itemView.findViewById(R.id.tv_like_count);
            layoutLike=itemView.findViewById(R.id.layout_like);
        }
        void bind(Post post,int position){
            if(post.getClips() != null &&!post.getClips().isEmpty()){
                Clip firstClip=post.getClips().get(0);
                int targetWidth = itemView.getWidth();
                if (targetWidth == 0) {
                    targetWidth = context.getResources().getDisplayMetrics().widthPixels - dp2px(24);
                }
                int targetHeight=calculateCoverHeight(
                        firstClip.getWidth(),
                        firstClip.getHeight(),
                        targetWidth
                );
                ViewGroup.LayoutParams params=ivCover.getLayoutParams();
                params.height=targetHeight;
                ivCover.setLayoutParams(params);
                Glide.with(context)
                        .load(firstClip.getUrl())
                        .transform(new CenterCrop(),new RoundedCorners(16))
                        .placeholder(R.color.gray_light)
                        .error(R.color.gray_light)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)  // 启用缓存，与预加载一致
                        .into(ivCover);
            }
            String displayText=!post.getTitle().isEmpty()?post.getTitle():post.getContent();
            tvTitle.setText(displayText);
            Author author=post.getAuthor();
            if(author !=null){
                tvNickname.setText(author.getNickname());
                Glide.with(context)
                        .load(author.getAvatar())
                        .circleCrop()
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .into(ivAvatar);
            }
            boolean isLiked=likeManager.isActive(post.getPostId());
            int baseCount = LikeCountGenerator.generateLikeCount(post.getPostId());
            int finalCount = isLiked ? baseCount + 1 : baseCount;
            tvLikeCount.setText(String.valueOf(finalCount));
            updateLikeUI(isLiked);
            layoutLike.setOnClickListener(v->{
                boolean newState=likeManager.toggleState(post.getPostId());
                updateLikeUI(newState);
                int currentCount=Integer.parseInt(tvLikeCount.getText().toString());
                if(newState){
                    tvLikeCount.setText(String.valueOf(currentCount + 1));
                    AnimationUtils.playLikeAnimation(ivLike);
                }else{
                    tvLikeCount.setText(String.valueOf(currentCount - 1));
                    AnimationUtils.playUnlikeAnimation(ivLike);
                }
            });
            itemView.setOnClickListener(v->{
                // ✅ 避免快速点击导致动画混乱
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (activity.isFinishing() || activity.isDestroyed()) {
                        return;
                    }
                }
                Intent intent=new Intent(context, PostDetailActivity.class);
                // ✅ 点击时也缓存一次（确保最新）
                DataManager.getInstance().cachePost(post);

                intent.putExtra("post_id", post.getPostId());
                // ✅ 使用共享元素转场动画
                if (context instanceof android.app.Activity) {
                    android.app.Activity activity = (android.app.Activity) context;

                    // 创建共享元素
                    android.app.ActivityOptions options = android.app.ActivityOptions
                            .makeSceneTransitionAnimation(
                                    activity,
                                    ivCover,  // 共享的 View
                                    "shared_image"  // transitionName
                            );

                    activity.startActivity(intent, options.toBundle());
                } else {
                    context.startActivity(intent);
                }

            });
        }
        private void updateLikeUI(boolean isLiked){
            if(isLiked){
                ivLike.setImageResource(R.drawable.ic_like_active);
            }else{
                ivLike.setImageResource(R.drawable.ic_like_normal);
            }
        }
        private int calculateCoverHeight(int originalWidth, int originalHeight, int targetWidth) {
            if (originalWidth == 0 || originalHeight == 0) return 600;

            float originalRatio = (float) originalWidth / originalHeight;
            float constrainedRatio = Math.max(0.75f, Math.min(1.33f, originalRatio));

            return (int) (targetWidth / constrainedRatio);
        }
        private int dp2px(int dp) {
            float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dp * scale + 0.5f);
        }

    }
}
