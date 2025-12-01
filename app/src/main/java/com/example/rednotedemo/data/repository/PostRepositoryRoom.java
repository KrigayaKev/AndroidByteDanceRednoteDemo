package com.example.rednotedemo.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.rednotedemo.data.database.AppDatabase;
import com.example.rednotedemo.data.dao.CommentDao;
import com.example.rednotedemo.data.dao.PostDao;
import com.example.rednotedemo.data.dao.PostImageDao;
import com.example.rednotedemo.data.dao.UserDao;
import com.example.rednotedemo.entity.Comment;
import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.PostImage;
import com.example.rednotedemo.entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostRepositoryRoom {
    private final PostDao postDao;
    private final PostImageDao postImageDao;
    private final UserDao userDao;
    private final CommentDao commentDao;
    private final ExecutorService executor;

    public PostRepositoryRoom(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        this.postDao = database.postDao();
        this.postImageDao = database.postImageDao();
        this.userDao = database.userDao();
        this.commentDao = database.commentDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    // Post相关方法
    public LiveData<Post> getPostById(int postId) {
        return postDao.getPostById(postId);
    }

    // PostImage相关方法
    public LiveData<List<PostImage>> getPostImages(int postId) {
        return postImageDao.getImagesByPostIdLiveData(postId);
    }

    // User相关方法
    public LiveData<User> getUserById(int userId) {
        return userDao.getUserByIdLiveData(userId);
    }

    // Comment相关方法
    public LiveData<List<Comment>> getCommentsByPostId(int postId) {
        return commentDao.getCommentsByPostId(postId);
    }

    public void insertComment(Comment comment) {
        executor.execute(() -> commentDao.insertComment(comment));
    }

    public void deleteComment(int commentId) {
        executor.execute(() -> commentDao.deleteComment(commentId));
    }
}