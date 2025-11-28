package com.example.rednotedemo.testdata;
import com.example.rednotedemo.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestDataGenerator {

  private static final String[] USERNAMES = {
     "小红薯", "旅行达人", "美食家", "摄影爱好者", "读书人",
     "运动健将", "时尚博主", "美妆达人", "程序员", "设计师",
     "音乐人", "画家", "作家", "老师", "学生"
  };

  private static final String[] AVATAR_URLS = {
     "https://example.com/avatar1.jpg",
     "https://example.com/avatar2.jpg",
     "https://example.com/avatar3.jpg",
     "https://example.com/avatar4.jpg",
     "https://example.com/avatar5.jpg"
  };

  private static final String PHONE_PREFIX = "138";

  /**
   * 生成单个测试用户
   */
  public static User generateTestUser() {
    Random random = new Random();
    String username = USERNAMES[random.nextInt(USERNAMES.length)] + random.nextInt(1000);
    String avatarUrl = AVATAR_URLS[random.nextInt(AVATAR_URLS.length)];

    User user = new User(username, avatarUrl);
    user.setPhone(PHONE_PREFIX + String.format("%08d", random.nextInt(100000000)));

    return user;
  }

  /**
   * 生成多个测试用户
   */
  public static List<User> generateTestUsers(int count) {
    List<User> users = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      users.add(generateTestUser());
    }
    return users;
  }
}