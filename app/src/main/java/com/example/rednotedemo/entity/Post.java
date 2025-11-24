package com.example.rednotedemo.entity;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
   tableName = "post",
   foreignKeys = @ForeignKey(
      entity = User.class,
      parentColumns = "id",
      childColumns = "user_id",
      onDelete = ForeignKey.CASCADE
   ),
   indices = {@Index("user_id")}
)
public class Post {

  @PrimaryKey(autoGenerate = true)
  private int id;

  private int userId;

  private String title;         // NOT NULL

  private String content;       // 可为 null

  private String videoUrl;      // 可为 null

  private boolean isVideo;      // 默认 false

  private String coverUrl;      // NOT NULL

  private long createTime;      // 时间戳（毫秒）

  private long updateTime;      // 时间戳（毫秒）

  // 构造方法
  public Post() {}

  // Getters and Setters
  public int getId() { return id; }
  public void setId(int id) { this.id = id; }

  public int getUserId() { return userId; }
  public void setUserId(int userId) { this.userId = userId; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }

  public String getVideoUrl() { return videoUrl; }
  public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

  public boolean isVideo() { return isVideo; }
  public void setVideo(boolean video) { isVideo = video; }

  public String getCoverUrl() { return coverUrl; }
  public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

  public long getCreateTime() { return createTime; }
  public void setCreateTime(long createTime) { this.createTime = createTime; }

  public long getUpdateTime() { return updateTime; }
  public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }
}