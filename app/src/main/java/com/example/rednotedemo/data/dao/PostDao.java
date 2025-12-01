// PostDao.java
package com.example.rednotedemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.PostImage;

import java.util.List;

@Dao
public interface PostDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insertPost(Post post);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertPostImages(List<PostImage> postImages);

  @Query("SELECT * FROM post ORDER BY create_time DESC")
  LiveData<List<Post>> getAllPosts();

  @Query("SELECT * FROM post_image WHERE post_id = :postId ORDER BY sort_order")
  List<PostImage> getPostImages(int postId);

  @Query("SELECT COUNT(*) FROM post")
  int getPostCount();

  @Query("SELECT * FROM post WHERE id = :postId")
  LiveData<Post> getPostById(int postId);

  @Query("SELECT * FROM post ORDER BY create_time DESC LIMIT :limit OFFSET :offset")
  List<Post> getPostsWithLimit(int limit, int offset);
}