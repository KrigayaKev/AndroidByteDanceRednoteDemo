package com.example.rednotedemo.presentation.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rednotedemo.R;
import com.example.rednotedemo.entity.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommentAdapter extends ListAdapter<Comment, CommentAdapter.ViewHolder> {

    private final SimpleDateFormat dateFormat;

    public CommentAdapter() {
        super(new CommentDiffCallback());
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = getItem(position);
        holder.bind(comment);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAvatar;
        private final TextView tvAuthorName;
        private final TextView tvContent;
        private final TextView tvTime;
        private final TextView tvLike;
        private final TextView tvReply;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvLike = itemView.findViewById(R.id.tv_like);
            tvReply = itemView.findViewById(R.id.tv_reply);
        }

        void bind(Comment comment) {
            tvContent.setText(comment.getContent());
            tvTime.setText(formatTime(comment.getCreateTime()));

            // 这里需要根据comment.getUserId()加载用户信息
            // 在实际应用中，你需要通过ViewModel获取用户信息并设置
            tvAuthorName.setText("用户" + comment.getUserId());

            // 设置点击事件
            tvLike.setOnClickListener(v -> {
                // 点赞逻辑
            });

            tvReply.setOnClickListener(v -> {
                // 回复逻辑
            });
        }

        private String formatTime(long timeStamp) {
            return dateFormat.format(new Date(timeStamp));
        }
    }

    static class CommentDiffCallback extends DiffUtil.ItemCallback<Comment> {
        @Override
        public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getContent().equals(newItem.getContent());
        }
    }
}