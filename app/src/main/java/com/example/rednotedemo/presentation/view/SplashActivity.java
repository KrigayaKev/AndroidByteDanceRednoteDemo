package com.example.rednotedemo.presentation.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rednotedemo.R;

public class SplashActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_splash);

    // 隐藏 ActionBar,实现真正的全屏效果
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }
    getWindow().getDecorView().setSystemUiVisibility(
       View.SYSTEM_UI_FLAG_FULLSCREEN |
          View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
          View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    );

    // 模拟初始化（如检查登录、加载配置等）
    new Handler().postDelayed(() -> {
      startActivity(new Intent(SplashActivity.this, MainActivity.class));
      finish();
    }, 2000); // 延迟 2 秒
    
  }
}