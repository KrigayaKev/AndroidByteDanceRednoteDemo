package com.example.rednotedemo.data.dao;

import com.example.rednotedemo.entity.Post;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Flowable;

@Dao
public interface PostDao {
  @Query("SELECT * FROM post ORDER BY create_time DESC")
  Flowable<List<Post>> getAllPosts();

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Post post);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<Post> posts);
}
