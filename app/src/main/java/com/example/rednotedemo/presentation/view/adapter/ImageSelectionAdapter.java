package com.example.rednotedemo.presentation.view.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.rednotedemo.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// adapter/ImageSelectionAdapter.java
public class ImageSelectionAdapter extends RecyclerView.Adapter<ImageSelectionAdapter.ViewHolder> {

  private List<Uri> imageUris;
  private OnImageClickListener listener;

  public interface OnImageClickListener {
    void onAddImage();
    void onDeleteImage(int position);
  }

  public ImageSelectionAdapter(OnImageClickListener listener) {
    this.imageUris = new ArrayList<>();
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_ADD) {
      View view = LayoutInflater.from(parent.getContext())
         .inflate(R.layout.item_add_image, parent, false);
      return new AddViewHolder(view);
    } else {
      View view = LayoutInflater.from(parent.getContext())
         .inflate(R.layout.item_selected_image, parent, false);
      return new ImageViewHolder(view);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    if (holder instanceof ImageViewHolder) {
      Uri uri = imageUris.get(position);
      Glide.with(holder.itemView.getContext())
         .load(uri)
         .into(((ImageViewHolder) holder).ivImage);
    }
  }

  @Override
  public int getItemViewType(int position) {
    return (position == imageUris.size()) ? VIEW_TYPE_ADD : VIEW_TYPE_IMAGE;
  }

  @Override
  public int getItemCount() {
    // 显示图片 + 一个“+”按钮（最多9张时不显示+）
    return Math.min(imageUris.size() + 1, 10); // 9张图 + 1个add = 10
  }

  public void addImage(Uri uri) {
    if (imageUris.size() < 9) {
      int position = imageUris.size(); // 新图片的位置
      imageUris.add(uri);
      notifyItemInserted(position); // 通知插入图片

      // 如果原来是 8 张（现在 9 张），需要移除“+”按钮
      if (imageUris.size() == 9) {
        notifyItemRemoved(position + 1); // 移除原来“+”的位置
      }
    }
  }

  public void removeImage(int position) {
    if (position >= 0 && position < imageUris.size()) {
      imageUris.remove(position);
      notifyItemRemoved(position);

      // 如果原来是 9 张（现在 8 张），需要重新显示“+”按钮
      if (imageUris.size() == 8) {
        notifyItemInserted(imageUris.size()); // 在末尾插入“+”
      }
    }
  }

  public List<Uri> getImageUris() {
    return new ArrayList<>(imageUris);
  }

  static final int VIEW_TYPE_IMAGE = 0;
  static final int VIEW_TYPE_ADD = 1;

  class ViewHolder extends RecyclerView.ViewHolder {
    ViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }

  class ImageViewHolder extends ViewHolder {
    ImageView ivImage, ivDelete;

    ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      ivImage = itemView.findViewById(R.id.ivImage);
      ivDelete = itemView.findViewById(R.id.ivDelete);
      ivDelete.setOnClickListener(v -> {
        int pos = getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION) {
          listener.onDeleteImage(pos);
        }
      });
    }
  }

  class AddViewHolder extends ViewHolder {
    AddViewHolder(@NonNull View itemView) {
      super(itemView);
      itemView.setOnClickListener(v -> listener.onAddImage());
    }
  }
}
