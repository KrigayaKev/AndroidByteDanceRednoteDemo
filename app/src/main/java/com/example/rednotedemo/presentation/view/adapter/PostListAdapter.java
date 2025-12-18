package com.example.rednotedemo.presentation.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.rednotedemo.R;
import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.presentation.view.PostDetailActivity;
import com.example.rednotedemo.presentation.view.viewholder.PostViewHolder;

public class PostListAdapter extends PagingDataAdapter<PostListItemVO, PostViewHolder> {

  private Context context;

  public PostListAdapter(MyComparator myComparator, Context mContext){
    super(myComparator);
    this.context = mContext;
  }

  public static class MyComparator extends DiffUtil.ItemCallback<PostListItemVO> {

    @Override
    public boolean areItemsTheSame(@NonNull PostListItemVO oldItem, @NonNull PostListItemVO newItem) {
      return oldItem.getPostId() == newItem.getPostId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull PostListItemVO oldItem, @NonNull PostListItemVO newItem) {
      return oldItem.equals(newItem);
    }
  }

  @NonNull
  @Override
  public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
       .inflate(R.layout.item_post, parent, false);
    return new PostViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
    PostListItemVO item = getItem(position);
    if (item == null) return;

    // 1. 封面图（Cover Image）
    String coverUrl = item.getCoverUrl();
    if (coverUrl != null && !coverUrl.isEmpty()) {
      Glide.with(holder.getImageView().getContext())
         .load(coverUrl)
         .placeholder(R.color.placeholder_gray) // 默认灰色占位
         .into(holder.getImageView());
    } else {
      // 如果 coverUrl 为 null 或空，使用本地资源
      holder.getImageView().setImageResource(R.drawable.rednotelogo);
    }

    // 2. 标题
    holder.getTextContent().setText(item.getTitle());

    // 3. 头像（Avatar）- 修复了加载逻辑
    String avatarUrl = item.getAuthorAvatarUrl();
    if (avatarUrl != null && !avatarUrl.isEmpty()) {
      Glide.with(holder.getAvatar().getContext())
         .load(avatarUrl) // 使用实际的网络头像URL
         .apply(RequestOptions.bitmapTransform(new CircleCrop()))
         .placeholder(R.drawable.qq_avatar) // 添加占位图
         .into(holder.getAvatar());
    } else {
      // 如果 avatarUrl 为 null 或空，使用本地 QQ 头像
      holder.getAvatar().setImageResource(R.drawable.qq_avatar);
    }

    // 4. 昵称
    holder.getTextAuthor().setText(item.getAuthorName());

    // 5. 点赞数
    holder.getTextLikes().setText(String.valueOf(item.getLikesCount()));

    final int currentPosition = position;
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PostListItemVO item = getItem(currentPosition);
        if (item != null) {
          // 跳转到详情页，并传递postId
          Intent intent = new Intent(context, PostDetailActivity.class);
          intent.putExtra("POST_ID", item.getPostId());
          context.startActivity(intent);
        }
      }
    });
  }
}