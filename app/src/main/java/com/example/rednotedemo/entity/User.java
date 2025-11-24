package com.example.rednotedemo.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {

  @PrimaryKey(autoGenerate = true)
  private int id;

  private String username;   // NOT NULL, UNIQUE

  private String avatarUrl;  // 头像 URL，可为 null

  private String phone;      // 手机号，可为 null

  private long createTime;   // 时间戳（毫秒）

  private long updateTime;   // 时间戳（毫秒）

  // 默认构造方法（Room 要求）
  public User() {}

  // Getters and Setters
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
  }
}