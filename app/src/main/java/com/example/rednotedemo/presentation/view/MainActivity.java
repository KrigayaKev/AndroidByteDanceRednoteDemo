package com.example.rednotedemo.presentation.view;

import android.os.Bundle;
import android.widget.TextView;

import com.example.rednotedemo.R;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

  private TextView[] tabs;
  private int currentIndex = 0;

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

    // 初始化底部 tab
    tabs = new TextView[]{
       findViewById(R.id.tab_home),
       findViewById(R.id.tab_market),
       findViewById(R.id.tab_message),
       findViewById(R.id.tab_me)
    };

    updateTabSelection(0); // 默认选中首页

    // 绑定点击事件
    for (int i = 0; i < tabs.length; i++) {
      final int index = i;
      tabs[i].setOnClickListener(v -> {
        if (index == currentIndex) return;
        updateTabSelection(index);
        // TODO: 根据 index 切换页面内容（如 Fragment 或 RecyclerView 数据）
      });
    }
  }

  private void updateTabSelection(int index) {
    tabs[currentIndex].setSelected(false);
    tabs[index].setSelected(true);
    currentIndex = index;
  }
}