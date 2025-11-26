package com.example.rednotedemo.data.repository;

import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.enums.FilterType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PostRepository {
  private List<PostListItemVO> posts = new ArrayList<>();
  private Random random = new Random();
  private static final int MAX_ID = 10000; // 假设最大 ID 为 10000

    // 模拟获取分页数据
    public List<PostListItemVO> getNeedPagingList(FilterType type, int loadSize, int offset) {
        List<PostListItemVO> items = new ArrayList<>();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 根据 offset 和 loadSize 生成对应页的数据
        for (int i = 0; i < loadSize; i++) {
            int id = random.nextInt(MAX_ID) + offset + i; // 确保 ID 唯一且按顺序递增

            PostListItemVO item = new PostListItemVO();
            item.setPostId(id);
            item.setCoverUrl("https://picsum.photos/300/400?random=" + id);
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
