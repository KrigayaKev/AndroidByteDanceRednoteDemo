package com.example.rednotedemo.entity.vo;

import com.example.rednotedemo.common.enums.FilterType;

import java.util.Objects;

public class PostListItemVO {

  private int postId;
  private String coverUrl;          // 封面图，必有
  private String authorAvatarUrl;   // 作者头像
  private String authorName;        // 作者昵称
  private String title;             // 标题，必有
  private int likesCount;           // 点赞数
  private FilterType type; // 新增筛选类型

  // 构造方法
  public PostListItemVO() {}

  // Getter 和 Setter（返回 this 实现链式调用）
  public PostListItemVO setPostId(int postId) {
    this.postId = postId;
    return this;
  }

  public PostListItemVO setCoverUrl(String coverUrl) {
    this.coverUrl = coverUrl;
    return this;
  }

  public PostListItemVO setAuthorAvatarUrl(String authorAvatarUrl) {
    this.authorAvatarUrl = authorAvatarUrl;
    return this;
  }

  public PostListItemVO setAuthorName(String authorName) {
    this.authorName = authorName;
    return this;
  }

  public PostListItemVO setTitle(String title) {
    this.title = title;
    return this;
  }

  public PostListItemVO setLikesCount(int likesCount) {
    this.likesCount = likesCount;
    return this;
  }
  
  public PostListItemVO setType(FilterType type) {
    this.type = type;
    return this;
  }

  // Getters（保持不变）
  public int getPostId() { return postId; }
  public String getCoverUrl() { return coverUrl; }
  public String getAuthorAvatarUrl() { return authorAvatarUrl; }
  public String getAuthorName() { return authorName; }
  public String getTitle() { return title; }
  public int getLikesCount() { return likesCount; }
  public FilterType getType() { return type; }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    PostListItemVO that = (PostListItemVO) o;
    return postId == that.postId && likesCount == that.likesCount && Objects.equals(coverUrl, that.coverUrl) && Objects.equals(authorAvatarUrl, that.authorAvatarUrl) && Objects.equals(authorName, that.authorName) && Objects.equals(title, that.title) && type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(postId, coverUrl, authorAvatarUrl, authorName, title, likesCount, type);
  }
}
