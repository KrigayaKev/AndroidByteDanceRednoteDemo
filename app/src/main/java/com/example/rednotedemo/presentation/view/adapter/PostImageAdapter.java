package com.example.rednotedemo.presentation.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rednotedemo.R;
import com.example.rednotedemo.entity.PostImage;

public class PostImageAdapter extends ListAdapter<PostImage, PostImageAdapter.ViewHolder> {

    public PostImageAdapter() {
        super(new ImageDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostImage postImage = getItem(position);
        holder.bind(postImage);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
        }

        void bind(PostImage postImage) {
            // 加载图片
            // Glide.with(ivImage).load(postImage.getImageUrl()).into(ivImage);
            ivImage.setContentDescription("图片: " + postImage.getImageUrl());
        }
    }

    static class ImageDiffCallback extends DiffUtil.ItemCallback<PostImage> {
        @Override
        public boolean areItemsTheSame(@NonNull PostImage oldItem, @NonNull PostImage newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull PostImage oldItem, @NonNull PostImage newItem) {
            return oldItem.getImageUrl().equals(newItem.getImageUrl());
        }
    }
}