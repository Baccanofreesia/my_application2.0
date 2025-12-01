package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.myapplication.R;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>{
    private Context context;
    private List<String> imageUrls;
    private int containerHeight;
    private OnImageLoadListener onImageLoadListener;
    public interface OnImageLoadListener {
        void onImageLoaded(int position, boolean success);
    }
    public ImagePagerAdapter(Context context,List<String>imageUrls,int containerHeight){
        this.context=context;
        this.imageUrls=imageUrls;
        this.containerHeight=containerHeight;
    }
    // ✅ 设置回调
    public void setOnImageLoadListener(OnImageLoadListener listener) {
        this.onImageLoadListener = listener;
    }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_image_pager,parent,false);
        return new ImageViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl=imageUrls.get(position);
        //关键：设置ImageView高度与容器一致（填满容器）
        ViewGroup.LayoutParams params=holder.imageView.getLayoutParams();
        params.height=containerHeight;
        holder.imageView.setLayoutParams(params);
        //关于加载中状态
        holder.pbLoading.setVisibility(View.VISIBLE);//显示加载动画
        holder.layoutError.setVisibility(View.GONE);
        holder.imageView.setVisibility(View.INVISIBLE);
        Glide.with(context)
             .load(imageUrl)
             .centerCrop()
             .placeholder(R.drawable.ic_placeholder)
             .error(R.drawable.ic_placeholder)
             .diskCacheStrategy(DiskCacheStrategy.ALL)//优化缓存
             .transition(DrawableTransitionOptions.withCrossFade(200))//选择，这里是动画
             .listener(new RequestListener<Drawable>(){//这是监听器
                 @Override
                 public boolean onLoadFailed(
                         @androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e,
                         Object model,
                         Target<Drawable> target,
                         boolean isFirstResource){
                     holder.pbLoading.setVisibility(View.GONE);
                     holder.layoutError.setVisibility(View.VISIBLE);
                     holder.imageView.setVisibility(View.INVISIBLE);
                     // ✅ 通知加载失败
                     if (onImageLoadListener != null) {
                         onImageLoadListener.onImageLoaded(position, false);
                     }

                     android.util.Log.e("ImageLoad", "图片 " + position + " 加载失败: " + imageUrl);
                     return false;
                 }
                 @Override
                  public boolean onResourceReady(
                          Drawable resource,
                          Object model,
                          Target<Drawable> target,
                          com.bumptech.glide.load.DataSource dataSource,
                          boolean isFirstResource){
                     holder.pbLoading.setVisibility(View.GONE);
                     holder.layoutError.setVisibility(View.GONE);
                     holder.imageView.setVisibility(View.VISIBLE);
                     // ✅ 通知加载成功
                     if (onImageLoadListener != null) {
                         onImageLoadListener.onImageLoaded(position, true);
                     }

                     android.util.Log.d("ImageLoad", "图片 " + position + " ✅ 加载成功 (来源: " + dataSource + ")");
                     return false;
                 }
             })
             .into(holder.imageView);
    }
    @Override
    public int getItemCount() {
        return imageUrls!=null?imageUrls.size():0;
    }
    class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ProgressBar pbLoading;
        LinearLayout layoutError;
        ImageViewHolder(@NonNull View itemView){
            super(itemView);
            imageView=itemView.findViewById(R.id.iv_image);
            pbLoading=itemView.findViewById(R.id.pb_loading);
            layoutError=itemView.findViewById(R.id.layout_error);
        }
    }
}
