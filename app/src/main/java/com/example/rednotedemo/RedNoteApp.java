package com.example.rednotedemo;

import android.app.Application;

import com.example.rednotedemo.data.database.AppDatabase;

/**
 * 应用初始化配置
 */
public class RedNoteApp extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    // 预初始化数据库，确保单例
    AppDatabase.getInstance(this);
  }

  @Override
  public void onTerminate() {
    // 只在应用完全退出时关闭数据库
    AppDatabase.getInstance(this).close();
    super.onTerminate();
  }
}