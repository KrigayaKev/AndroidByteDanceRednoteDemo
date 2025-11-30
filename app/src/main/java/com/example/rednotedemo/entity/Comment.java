package com.example.rednotedemo.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "comment",
        foreignKeys = {
                @ForeignKey(
                        entity = Post.class,
                        parentColumns = "id",
                        childColumns = "post_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class Comment {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "post_id")
    private int postId;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "parent_comment_id")
    private Integer parentCommentId;

    @ColumnInfo(name = "create_time")
    private long createTime;

    // 构造方法
    public Comment(int postId, int userId, String content) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Integer parentCommentId) { this.parentCommentId = parentCommentId; }

    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
}