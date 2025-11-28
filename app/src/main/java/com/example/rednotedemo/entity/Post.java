package com.example.rednotedemo.entity;


import androidx.room.ColumnInfo;
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

  @ColumnInfo(name = "user_id")
  private int userId;

  @ColumnInfo(name = "title")
  private String title;

  @ColumnInfo(name = "content")
  private String content;

  @ColumnInfo(name = "video_url")
  private String videoUrl;

  @ColumnInfo(name = "is_video")
  private boolean isVideo;

  @ColumnInfo(name = "cover_url")
  private String coverUrl;

  @ColumnInfo(name = "create_time")
  private long createTime;

  @ColumnInfo(name = "update_time")
  private long updateTime;

  // 构造方法
  public Post() {}

  public Post(int id, int userId, String title, String content, String videoUrl, boolean isVideo, String coverUrl, long createTime, long updateTime) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.content = content;
    this.videoUrl = videoUrl;
    this.isVideo = isVideo;
    this.coverUrl = coverUrl;
  }

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