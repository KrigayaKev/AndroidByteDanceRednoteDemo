package com.example.rednotedemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.rednotedemo.entity.Comment;

import java.util.List;

@Dao
public interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertComment(Comment comment);

    @Query("SELECT * FROM comment WHERE post_id = :postId ORDER BY create_time DESC")
    LiveData<List<Comment>> getCommentsByPostId(int postId);

    @Query("SELECT COUNT(*) FROM comment WHERE post_id = :postId")
    LiveData<Integer> getCommentCountByPostId(int postId);

    @Query("DELETE FROM comment WHERE id = :commentId")
    void deleteComment(int commentId);

    @Query("SELECT * FROM comment WHERE id = :commentId")
    Comment getCommentById(int commentId);
}