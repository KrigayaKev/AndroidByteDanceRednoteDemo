package com.example.rednotedemo.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import static androidx.room.ForeignKey.CASCADE;
import androidx.room.PrimaryKey;

@Entity(tableName = "post_image", foreignKeys = @ForeignKey(
   entity = Post.class,
   parentColumns = "id",
   childColumns = "post_id",
   onDelete = CASCADE
))
public class PostImage {
  @PrimaryKey(autoGenerate = true)
  private int id;

  @ColumnInfo(name = "post_id")
  private int postId;

  @ColumnInfo(name = "image_url")
  private String imageUrl;

  @ColumnInfo(name = "sort_order")
  private int sortOrder;

  @ColumnInfo(name = "create_time")
  private long createTime;

  // 构造函数、getter/setter
  public PostImage(int postId, String imageUrl, int sortOrder) {
    this.postId = postId;
    this.imageUrl = imageUrl;
    this.sortOrder = sortOrder;
    this.createTime = System.currentTimeMillis();
  }

  // Getters and Setters...

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getPostId() {
    return postId;
  }

  public void setPostId(int postId) {
    this.postId = postId;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
}
