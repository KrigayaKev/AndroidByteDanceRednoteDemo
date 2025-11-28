package com.example.rednotedemo.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.rednotedemo.data.dao.PostDao;
import com.example.rednotedemo.data.dao.UserDao;
import com.example.rednotedemo.entity.User;
import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.PostImage;

@Database(
   entities = {User.class, Post.class, PostImage.class}, // 确保包含所有实体
   version = 2,
   exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

  private static volatile AppDatabase INSTANCE;

  public abstract UserDao userDao();
  
  public abstract PostDao postDao();

  /**
   * 获取数据库实例（单例模式）
   */
  public static AppDatabase getInstance(Context context) {
    if (INSTANCE == null) {
      synchronized (AppDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(
             context.getApplicationContext(),
             AppDatabase.class,
             "rednote_database"
          ).fallbackToDestructiveMigration().build();
        }
      }
    }
    return INSTANCE;
  }

  /**
   * 获取内存数据库实例（用于测试）
   */
  public static AppDatabase getTestInstance(Context context) {
    return Room.inMemoryDatabaseBuilder(
       context.getApplicationContext(),
       AppDatabase.class
    ).build();
  }
}