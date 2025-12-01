// ImageSelectionAdapter.java
package com.example.rednotedemo.presentation.view.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rednotedemo.R;

import java.util.ArrayList;
import java.util.List;

public class ImageSelectionAdapter extends RecyclerView.Adapter<ImageSelectionAdapter.ViewHolder> {

  private static final int TYPE_ADD = 0;
  private static final int TYPE_IMAGE = 1;
  private static final int MAX_IMAGES = 9;

  private List<Uri> imageUris = new ArrayList<>();
  private OnImageClickListener listener;

  public interface OnImageClickListener {
    void onAddImage();
    void onDeleteImage(int position);
  }

  public ImageSelectionAdapter(OnImageClickListener listener) {
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    if (viewType == TYPE_ADD) {
      View view = inflater.inflate(R.layout.item_add_image, parent, false);
      return new AddViewHolder(view);
    } else {
      View view = inflater.inflate(R.layout.item_selected_image, parent, false);
      return new ImageViewHolder(view);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    if (holder.getItemViewType() == TYPE_ADD) {
      holder.itemView.setOnClickListener(v -> {
        if (listener != null) {
          listener.onAddImage();
        }
      });
    } else {
      ImageViewHolder imageHolder = (ImageViewHolder) holder;
      int imagePosition = position;

      // 安全处理：检查位置是否有效
      if (imagePosition >= 0 && imagePosition < imageUris.size()) {
        Uri uri = imageUris.get(imagePosition);
        Glide.with(imageHolder.itemView.getContext())
           .load(uri)
           .centerCrop()
           .into(imageHolder.ivImage);

        imageHolder.ivDelete.setOnClickListener(v -> {
          if (listener != null) {
            listener.onDeleteImage(imagePosition);
          }
        });
      }
    }
  }

  @Override
  public int getItemCount() {
    // 如果有添加按钮，总数量是图片数量 + 1
    if (imageUris.size() < MAX_IMAGES) {
      return imageUris.size() + 1;
    } else {
      return imageUris.size();
    }
  }

  @Override
  public int getItemViewType(int position) {
    // 如果图片数量未达上限，最后一个位置是添加按钮
    if (imageUris.size() < MAX_IMAGES && position == imageUris.size()) {
      return TYPE_ADD;
    }
    return TYPE_IMAGE;
  }

  public void addImage(Uri uri) {
    if (imageUris.size() < MAX_IMAGES) {
      imageUris.add(uri);
      notifyDataSetChanged(); // 使用 notifyDataSetChanged 确保位置正确
    }
  }

  public void removeImage(int position) {
    if (position >= 0 && position < imageUris.size()) {
      imageUris.remove(position);
      notifyDataSetChanged(); // 使用 notifyDataSetChanged 确保位置正确
    }
  }

  public void clearImages() {
    imageUris.clear();
    notifyDataSetChanged();
  }

  public List<Uri> getImageUris() {
    return new ArrayList<>(imageUris);
  }

  public int getCurrentImageCount() {
    return imageUris.size();
  }

  public boolean canAddMoreImages() {
    return imageUris.size() < MAX_IMAGES;
  }

  abstract static class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }

  static class AddViewHolder extends ViewHolder {
    public AddViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }

  static class ImageViewHolder extends ViewHolder {
    ImageView ivImage;
    ImageView ivDelete;

    public ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      ivImage = itemView.findViewById(R.id.ivImage);
      ivDelete = itemView.findViewById(R.id.ivDelete);
    }
  }
}