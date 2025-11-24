package com.example.rednotedemo.presentation.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.rednotedemo.R;
import com.example.rednotedemo.presentation.view.adapter.MainPagerAdapter;
import com.example.rednotedemo.presentation.view.fragment.HomeFragment;
import com.example.rednotedemo.presentation.view.fragment.PlaceholderFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.EdgeToEdge;
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

    // 初始化 ViewPager2
    viewPager = findViewById(R.id.viewPager);

    // 创建 Fragment 列表：0=首页，1=占位页（用于其他所有 tab）
    fragments = new ArrayList<>();
    fragments.add(new HomeFragment()); // 首页
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
        updateTabSelection(position);
        if (position == 0) {
          // 首页：切换到第 0 页
          viewPager.setCurrentItem(0, false);
        } else {
          // 其他：切换到第 1 页（占位页）
          viewPager.setCurrentItem(1, false);
          // 可选：弹出 Toast（避免用户疑惑）
          // Toast.makeText(MainActivity.this, "该功能暂未实现", Toast.LENGTH_SHORT).show();
        }
      });
    }

    // 默认选中首页
    viewPager.setCurrentItem(0, false);
    // 初始化时选中首页,添加选中效果
    updateTabSelection(0);
  }
  private void updateTabSelection(int index) {
    tabViews[currentIndex].setSelected(false);
    tabViews[index].setSelected(true);
    currentIndex = index;
  }
}