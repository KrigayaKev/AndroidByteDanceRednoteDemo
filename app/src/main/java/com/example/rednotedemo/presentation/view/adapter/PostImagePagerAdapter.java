package com.example.rednotedemo.presentation.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rednotedemo.R;
import com.example.rednotedemo.entity.PostImage;

import java.util.List;

public class PostImagePagerAdapter extends RecyclerView.Adapter<PostImagePagerAdapter.ViewHolder> {

  private List<PostImage> imageList;

  public PostImagePagerAdapter(List<PostImage> imageList) {
    this.imageList = imageList;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
       .inflate(R.layout.item_image_pager, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    PostImage postImage = imageList.get(position);
    holder.bind(postImage);
  }

  @Override
  public int getItemCount() {
    return imageList != null ? imageList.size() : 0;
  }

  public void setImageList(List<PostImage> imageList) {
    this.imageList = imageList;
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    private final ImageView ivImage;

    ViewHolder(View itemView) {
      super(itemView);
      ivImage = itemView.findViewById(R.id.iv_image);
    }

    void bind(PostImage postImage) {
      // 加载图片
      Glide.with(ivImage)
         .load(postImage.getImageUrl())
         .placeholder(R.color.gray_200)
         .centerCrop()
         .into(ivImage);

      ivImage.setContentDescription("图片: " + postImage.getImageUrl());
    }
  }
}