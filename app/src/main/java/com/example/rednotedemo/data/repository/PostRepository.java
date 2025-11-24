package com.example.rednotedemo.data.repository;

import com.example.rednotedemo.entity.vo.PostListItemVO;

import java.util.ArrayList;
import java.util.List;

public class PostRepository {
  private List<PostListItemVO> posts = new ArrayList<>();

  public void loadPosts() {
    // 模拟加载数据（实际应从网络或数据库获取）
    posts.clear();
    posts.add(new PostListItemVO()
       .setPostId(1)
       .setCoverUrl("https://picsum.photos/300/400?random=1")
       .setAuthorName("一只开心")
       .setAuthorAvatarUrl("https://picsum.photos/50?random=1")
       .setTitle("在广州会常去的一家咖啡店")
       .setLikesCount(20));

    posts.add(new PostListItemVO()
       .setPostId(2)
       .setCoverUrl("https://picsum.photos/300/500?random=2")
       .setAuthorName("我叉又又槽了")
       .setAuthorAvatarUrl("https://picsum.photos/50?random=2")
       .setTitle("突然发现小狗竟然有嘴唇子！")
       .setLikesCount(670));

    posts.add(new PostListItemVO()
       .setPostId(3)
       .setCoverUrl("https://picsum.photos/300/350?random=3")
       .setAuthorName("禾禾小宝贝")
       .setAuthorAvatarUrl("https://picsum.photos/50?random=3")
       .setTitle("发现一个奇怪的现象：6个月的宝宝在家天天嘎嘎乐...")
       .setLikesCount(21));
  }

  public List<PostListItemVO> getPosts() {
    return posts;
  }
}
