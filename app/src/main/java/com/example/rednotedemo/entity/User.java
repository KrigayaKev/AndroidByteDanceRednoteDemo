package com.example.rednotedemo.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// model/User.java
@Entity(tableName = "user")
public class User {
  @PrimaryKey(autoGenerate = true)
  private int id;

  @ColumnInfo(name = "username")
  private String username;

  @ColumnInfo(name = "avatar_url")
  private String avatarUrl;

  @ColumnInfo(name = "phone")
  private String phone;

  @ColumnInfo(name = "create_time")
  private long createTime;

  @ColumnInfo(name = "update_time")
  private long updateTime;

  // 构造函数、getter/setter
  public User(String username, String avatarUrl) {
    this.username = username;
    this.avatarUrl = avatarUrl;
    this.createTime = System.currentTimeMillis();
    this.updateTime = this.createTime;
  }

  // Getters and Setters...

  public int getId() {
    return id;
  }

  public User setId(int id) {
    this.id = id;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public User setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public User setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
    return this;
  }

  public String getPhone() {
    return phone;
  }

  public User setPhone(String phone) {
    this.phone = phone;
    return this;
  }

  public long getCreateTime() {
    return createTime;
  }

  public User setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public User setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
    return this;
  }
}