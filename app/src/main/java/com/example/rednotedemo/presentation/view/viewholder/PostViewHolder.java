package com.example.rednotedemo.presentation.view.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rednotedemo.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 标题     :
 * 逻辑简介  :
 *
 * @author :yangpf
 * @date :2025/11/24  14:41
 */
public class PostViewHolder extends RecyclerView.ViewHolder {
  private ImageView imageView;
  private TextView textContent;
  private ImageView avatar;
  private TextView textAuthor;
  private TextView textLikes;

  public PostViewHolder(@NonNull View itemView) {
    super(itemView);
    imageView = itemView.findViewById(R.id.imageView);
    textContent = itemView.findViewById(R.id.textContent);
    avatar = itemView.findViewById(R.id.avatar);
    textAuthor = itemView.findViewById(R.id.textAuthor);
    textLikes = itemView.findViewById(R.id.textLikes);
  }

  public ImageView getImageView() {
    return imageView;
  }

  public PostViewHolder setImageView(ImageView imageView) {
    this.imageView = imageView;
    return this;
  }

  public TextView getTextContent() {
    return textContent;
  }

  public PostViewHolder setTextContent(TextView textContent) {
    this.textContent = textContent;
    return this;
  }

  public ImageView getAvatar() {
    return avatar;
  }

  public PostViewHolder setAvatar(ImageView avatar) {
    this.avatar = avatar;
    return this;
  }

  public TextView getTextAuthor() {
    return textAuthor;
  }

  public PostViewHolder setTextAuthor(TextView textAuthor) {
    this.textAuthor = textAuthor;
    return this;
  }

  public TextView getTextLikes() {
    return textLikes;
  }

  public PostViewHolder setTextLikes(TextView textLikes) {
    this.textLikes = textLikes;
    return this;
  }
}
