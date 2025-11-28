package com.example.rednotedemo.presentation.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.rednotedemo.R;
import com.example.rednotedemo.entity.vo.PostListItemVO;
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
       .inflate(R.layout.post_item, parent, false);
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
      holder.getImageView().setImageResource(R.drawable.rednotelogo); // 👈 使用你的 logo
    }



    // 2. 标题
    holder.getTextContent().setText(item.getTitle());

    // 3. 头像（Avatar）
    String avatarUrl = item.getAuthorAvatarUrl();
    if (avatarUrl != null && !avatarUrl.isEmpty()) {
      Glide.with(holder.getAvatar().getContext())
         .load("file:///android_asset/img/avatar7.png")
         .apply(RequestOptions.bitmapTransform(new CircleCrop()))
         .into(holder.getAvatar());
    } else {
//      // 如果 avatarUrl 为 null 或空，使用本地 QQ 头像
      holder.getAvatar().setImageResource(R.drawable.qq_avatar); // 👈 使用你的 QQ 头像
    }

    // 4. 昵称
    holder.getTextAuthor().setText(item.getAuthorName());

    // 5. 点赞数
    holder.getTextLikes().setText(String.valueOf(item.getLikesCount()));
  }
}