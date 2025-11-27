package com.example.rednotedemo.data.dao;

import com.example.rednotedemo.entity.PostImage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PostImageDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(PostImage image);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<PostImage> images);

  @Query("SELECT * FROM post_image WHERE post_id = :postId ORDER BY sort_order")
  List<PostImage> getImagesByPostId(int postId);
}
