package com.example.rednotedemo.data.repository;

import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.common.enums.FilterType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PostRepository {
  private List<PostListItemVO> posts = new ArrayList<>();
  private final Random random = new Random();
  private static final int MAX_ID = 10000; // 假设最大 ID 为 10000

    // 模拟获取分页数据
    public List<PostListItemVO> getNeedPagingList(FilterType type, int loadSize, int offset) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      // 模拟 50% 概率失败
      if (random.nextDouble() < 0.1) {
        throw new RuntimeException("网络连接失败，请检查您的网络");
      }
      List<PostListItemVO> items = new ArrayList<>();

        // 根据 offset 和 loadSize 生成对应页的数据
        for (int i = 0; i < loadSize; i++) {
            int id = random.nextInt(MAX_ID) + offset + i; // 确保 ID 唯一且按顺序递增

            PostListItemVO item = new PostListItemVO();
            item.setPostId(id);
            item.setCoverUrl("https://gitee.com/Haluzzz/my-images/raw/master/bytedance/IMG_20250713_161437.jpg");
            item.setTitle("动态 " + id);
            item.setAuthorName("用户" + id);
            item.setLikesCount(10 + (id % 100));
            item.setType(type); // 可以根据 type 过滤或标记不同内容类型

            items.add(item);
        }

        return items;
    }

  public List<PostListItemVO> getPosts() {
    return posts;
  }
}
