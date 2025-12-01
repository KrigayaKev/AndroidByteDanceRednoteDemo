package com.example.rednotedemo.data.repository;

import android.app.Application;

import com.example.rednotedemo.data.database.AppDatabase;
import com.example.rednotedemo.data.dao.PostDao;
import com.example.rednotedemo.data.dao.UserDao;
import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.common.enums.FilterType;
import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PostPageRepository {
  private PostDao postDao;
  private UserDao userDao;
  private final Random random = new Random();

  public PostPageRepository(Application application) {
    AppDatabase database = AppDatabase.getInstance(application);
    this.postDao = database.postDao();
    this.userDao = database.userDao();
  }

  // 从数据库获取分页数据
  public List<PostListItemVO> getNeedPagingList(FilterType type, int loadSize, int offset) {
    try {
      // 模拟网络延迟（实际开发中可以移除）
      Thread.sleep(500);

      // 模拟 10% 概率失败（实际开发中可以移除）
      if (random.nextDouble() < 0.1) {
        throw new RuntimeException("网络连接失败，请检查您的网络");
      }

      List<PostListItemVO> items = new ArrayList<>();

      // 从数据库获取帖子数据
      List<Post> posts = postDao.getPostsWithLimit(loadSize, offset);

      if (posts != null && !posts.isEmpty()) {
        for (Post post : posts) {
          // 获取用户信息
          User user = userDao.getUserById(post.getUserId());

          PostListItemVO item = new PostListItemVO();
          item.setPostId(post.getId());
          item.setCoverUrl(post.getCoverUrl());
          item.setTitle(post.getTitle());

          if (user != null) {
            item.setAuthorName(user.getUsername());
            item.setAuthorAvatarUrl(user.getAvatarUrl());
          } else {
            item.setAuthorName("未知用户");
            item.setAuthorAvatarUrl("");
          }

          // 点赞数暂时使用随机数（实际开发中应该从数据库获取）
          item.setLikesCount(10 + (post.getId() % 100));
          item.setType(type);

          items.add(item);
        }
      }

      return items;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("数据加载被中断", e);
    } catch (Exception e) {
      throw new RuntimeException("数据加载失败: " + e.getMessage(), e);
    }
  }

  // 检查数据库是否为空
  public boolean isDatabaseEmpty() {
    try {
      return postDao.getPostCount() == 0;
    } catch (Exception e) {
      return true;
    }
  }

  // 获取数据库中的帖子总数
  public int getTotalPostCount() {
    try {
      return postDao.getPostCount();
    } catch (Exception e) {
      return 0;
    }
  }
}