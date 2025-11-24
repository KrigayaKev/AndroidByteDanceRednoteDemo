package com.example.rednotedemo.presentation.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.rednotedemo.R;
import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.presentation.view.viewholder.PostViewHolder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostListAdapter extends RecyclerView.Adapter<PostViewHolder> {

  private List<PostListItemVO> dataList = new ArrayList<>();

  public PostListAdapter() {
    // 推荐使用无参构造，通过 setData 更新数据
  }
  
  public PostListAdapter(List<PostListItemVO> dataList) {
    this.dataList = dataList;
  }

  @NonNull
  @Override
  public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
       .inflate(R.layout.post_item, parent, false);
    return new PostViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
    if (position >= dataList.size()) return; // 防止越界

    PostListItemVO item = dataList.get(position);

    // 1. 封面图
    Glide.with(holder.getImageView().getContext())
       .load(item.getCoverUrl())
       .placeholder(R.color.placeholder_gray)
       .into(holder.getImageView());

    // 2. 标题/正文
    holder.getTextContent().setText(item.getTitle());

    // 3. 头像（圆形）
    Glide.with(holder.getAvatar().getContext())
       .load(item.getAuthorAvatarUrl())
       .apply(RequestOptions.bitmapTransform(new CircleCrop()))
       .placeholder(R.drawable.ic_default_avatar)
       .into(holder.getAvatar());

    // 4. 昵称
    holder.getTextAuthor().setText(item.getAuthorName());

    // 5. 点赞数
    holder.getTextLikes().setText(String.valueOf(item.getLikesCount()));
  }

  @Override
  public int getItemCount() {
    return dataList != null ? dataList.size() : 0;
  }

  // ✅ 新增：安全更新数据的方法（用于下拉刷新、分页加载等）
  public void updateData(@NonNull List<PostListItemVO> newData) {
    this.dataList.clear();
    this.dataList.addAll(newData);
    notifyDataSetChanged();
  }

  // 可选：增量更新（用于局部刷新）
  public void appendData(@NonNull List<PostListItemVO> newData) {
    int start = this.dataList.size();
    this.dataList.addAll(newData);
    notifyItemRangeInserted(start, newData.size());
  }
}