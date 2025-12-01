package com.example.rednotedemo.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import com.example.rednotedemo.data.dao.CommentDao;
import com.example.rednotedemo.data.dao.PostDao;
import com.example.rednotedemo.data.dao.PostImageDao;
import com.example.rednotedemo.data.dao.UserDao;
import com.example.rednotedemo.entity.Comment;
import com.example.rednotedemo.entity.User;
import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.PostImage;

@Database(
   entities = {User.class, Post.class, PostImage.class, Comment.class},
   version = 4,  // 版本号增加
   exportSchema = false
)
@TypeConverters({}) // 如果有类型转换器可以添加
public abstract class AppDatabase extends RoomDatabase {

  private static volatile AppDatabase INSTANCE;

  public abstract UserDao userDao();

  public abstract PostDao postDao();
  public abstract CommentDao commentDao();
  public abstract PostImageDao postImageDao();

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
             )
             .addMigrations(MIGRATION_3_4) // 注意：这会删除旧数据
             .build();
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

  // 在 AppDatabase.java 同级目录创建 Migration
  static final Migration MIGRATION_3_4 = new Migration(3, 4) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      // 添加 video_duration 字段
      database.execSQL("ALTER TABLE post ADD COLUMN video_duration INTEGER NOT NULL DEFAULT 0");
    }
  };
}