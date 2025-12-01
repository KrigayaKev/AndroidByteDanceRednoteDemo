package com.example.rednotedemo.presentation.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.rednotedemo.R;
import com.example.rednotedemo.data.dao.UserDao;
import com.example.rednotedemo.data.database.AppDatabase;
import com.example.rednotedemo.entity.User;
import com.example.rednotedemo.presentation.view.adapter.MainPagerAdapter;
import com.example.rednotedemo.presentation.view.fragment.HomeFragment;
import com.example.rednotedemo.presentation.view.fragment.PlaceholderFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

  private ViewPager2 viewPager;
  private List<Fragment> fragments;
  private View[] tabViews; // 支持 TextView 和 ImageView
  int currentIndex = 0;
  private AppDatabase database;
  private HomeFragment homeFragment;

  // 使用 ActivityResultLauncher 替代 startActivityForResult
  private ActivityResultLauncher<Intent> publishActivityLauncher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_main);

    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    // 初始化数据库
    database = AppDatabase.getInstance(this);

    // 异步插入初始数据
    insertInitialDataAsync();

    // 初始化 ActivityResultLauncher
    initPublishActivityLauncher();

    // 初始化 ViewPager2
    viewPager = findViewById(R.id.viewPager);

    // 创建 Fragment 列表：0=首页，1=占位页（用于其他所有 tab）
    fragments = new ArrayList<>();
    homeFragment = new HomeFragment(); // 保存 HomeFragment 引用
    fragments.add(homeFragment); // 首页
    fragments.add(PlaceholderFragment.newInstance("该功能暂未实现")); // 占位页

    MainPagerAdapter adapter = new MainPagerAdapter(this, fragments);
    viewPager.setAdapter(adapter);
    viewPager.setUserInputEnabled(false); // 禁止滑动切换

    // 初始化底部所有可点击视图（5个）
    tabViews = new View[]{
       findViewById(R.id.tab_home),
       findViewById(R.id.tab_market),
       findViewById(R.id.tab_add),      // ImageView
       findViewById(R.id.tab_message),
       findViewById(R.id.tab_me)
    };

    // 绑定点击事件
    for (int i = 0; i < tabViews.length; i++) {
      final int position = i;
      tabViews[i].setOnClickListener(v -> {
        if(position == currentIndex) {
          return;
        }
        if (position == 0) {
          // 首页：切换到第 0 页
          viewPager.setCurrentItem(0, false);
          updateTabSelection(position);
        } else if(position == 2){
          // 跳转到发布页，使用 ActivityResultLauncher
          navigateToPublish();
        } else {
          // 其他：切换到第 1 页（占位页）
          updateTabSelection(position);
          viewPager.setCurrentItem(1, false);
        }
      });
    }

    // 默认选中首页
    viewPager.setCurrentItem(0, false);
    // 初始化时选中首页,添加选中效果
    updateTabSelection(0);
  }

  /**
   * 初始化发布页的 ActivityResultLauncher
   */
  private void initPublishActivityLauncher() {
    publishActivityLauncher = registerForActivityResult(
       new ActivityResultContracts.StartActivityForResult(),
       new ActivityResultCallback<ActivityResult>() {
         @Override
         public void onActivityResult(ActivityResult result) {
           if (result.getResultCode() == RESULT_OK) {
             // 发布成功，切换到首页并刷新数据
             switchToHomeAndRefresh();
           }
         }
       });
  }

  /**
   * 跳转到发布页的公共方法
   */
  public void navigateToPublish() {
    Intent intent = new Intent(MainActivity.this, PublishActivity.class);
    publishActivityLauncher.launch(intent);
  }

  /**
   * 切换到首页并刷新数据
   */
  public void switchToHomeAndRefresh() {
    // 切换到首页
    viewPager.setCurrentItem(0, false);
    updateTabSelection(0);

    // 刷新 HomeFragment 数据
    if (homeFragment != null) {
      homeFragment.refreshData();
    }
  }

  private void updateTabSelection(int index) {
    tabViews[currentIndex].setSelected(false);
    tabViews[index].setSelected(true);
    currentIndex = index;
  }

  /**
   * 异步插入初始用户数据
   */
  private void insertInitialDataAsync() {
    Executors.newSingleThreadExecutor().execute(() -> {
      try {
        UserDao userDao = database.userDao();

        // 检查是否已有数据
        int userCount = userDao.getUsersCount();
        if (userCount > 0) {
          Log.d("MainActivity", "数据库已有 " + userCount + " 个用户，跳过初始化");
          return;
        }

        // 生成20个初始用户
        List<User> initialUsers = generateInitialUsers();
        List<Long> userIds = userDao.insertAll(initialUsers);

        Log.d("MainActivity", "成功插入 " + userIds.size() + " 个初始用户");

      } catch (Exception e) {
        Log.e("MainActivity", "插入初始数据失败", e);
      }
    });
  }

  /**
   * 生成20个初始用户数据
   */
  private List<User> generateInitialUsers() {
    List<User> users = new ArrayList<>();

    String[] usernames = {
       "小红薯", "旅行达人", "美食家", "摄影爱好者", "读书人",
       "运动健将", "时尚博主", "美妆达人", "程序员", "设计师",
       "音乐人", "画家", "作家", "老师", "学生",
       "旅行者", "美食探店", "摄影大师", "书虫", "健身教练"
    };

    // 假设您在 assets/img/ 目录下有 avatar1.png 到 avatar20.png
    for (int i = 0; i < usernames.length; i++) {
      int avatarIndex = (i % 10) + 1; // 循环使用头像 1-10

      User user = new User(
         usernames[i],
         "file:///android_asset/img/avatar" + avatarIndex + ".png"
      );

      // 设置手机号
      user.setPhone("138" + String.format("%08d", i + 1));

      // 设置创建和更新时间
      long currentTime = System.currentTimeMillis();
      user.setCreateTime(currentTime);
      user.setUpdateTime(currentTime);

      users.add(user);
    }

    return users;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}