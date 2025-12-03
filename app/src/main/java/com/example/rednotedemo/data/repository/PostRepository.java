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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostRepository {
  private final PostDao postDao;
  private final PostImageDao postImageDao;
  private final UserDao userDao;
  private final CommentDao commentDao;
  private final ExecutorService executor;

  public PostRepository(Application application) {
    AppDatabase database = AppDatabase.getInstance(application);
    this.postDao = database.postDao();
    this.postImageDao = database.postImageDao();
    this.userDao = database.userDao();
    this.commentDao = database.commentDao();
    this.executor = Executors.newSingleThreadExecutor();
  }

  // ========== Post相关方法 ==========
  public LiveData<Post> getPostById(int postId) {
    return postDao.getPostById(postId);
  }

  /**
   * 插入图文帖子
   */
  public long insertImagePost(Post post, List<PostImage> postImages) {
    try {
      // 1. 插入Post
      long postId = postDao.insertPost(post);

      // 2. 设置图片的postId并插入
      if (postImages != null && !postImages.isEmpty()) {
        for (PostImage image : postImages) {
          image.setPostId((int) postId);
        }
        postDao.insertPostImages(postImages);
      }

      return postId;
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * 插入视频帖子
   */
  public long insertVideoPost(Post post) {
    try {
      return postDao.insertPost(post);
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * 异步插入图文帖子
   */
  public void insertImagePostAsync(Post post, List<PostImage> postImages, InsertCallback callback) {
    executor.execute(() -> {
      try {
        long postId = insertImagePost(post, postImages);
        if (callback != null) {
          callback.onSuccess(postId);
        }
      } catch (Exception e) {
        if (callback != null) {
          callback.onError(e);
        }
      }
    });
  }

  /**
   * 异步插入视频帖子
   */
  public void insertVideoPostAsync(Post post, InsertCallback callback) {
    executor.execute(() -> {
      try {
        long postId = insertVideoPost(post);
        if (callback != null) {
          callback.onSuccess(postId);
        }
      } catch (Exception e) {
        if (callback != null) {
          callback.onError(e);
        }
      }
    });
  }

  // ========== PostImage相关方法 ==========
  public LiveData<List<PostImage>> getPostImages(int postId) {
    return postImageDao.getImagesByPostIdLiveData(postId);
  }

  // ========== User相关方法 ==========
  public LiveData<User> getUserById(int userId) {
    return userDao.getUserByIdLiveData(userId);
  }

  // ========== Comment相关方法 ==========
  public LiveData<List<Comment>> getCommentsByPostId(int postId) {
    return commentDao.getCommentsByPostId(postId);
  }

  public void insertComment(Comment comment) {
    executor.execute(() -> commentDao.insertComment(comment));
  }

  public void deleteComment(int commentId) {
    executor.execute(() -> commentDao.deleteComment(commentId));
  }

  // ========== 回调接口 ==========
  public interface InsertCallback {
    void onSuccess(long postId);
    void onError(Exception e);
  }

  // ========== 创建Post对象的方法 ==========

  /**
   * 创建图文Post对象
   */
  public Post createImagePost(String title, String content, String coverUrl, int userId) {
    Post post = new Post();
    post.setUserId(userId);
    post.setTitle(title);
    post.setContent(content);
    post.setVideoUrl(null);
    post.setVideo(false);
    post.setCoverUrl(coverUrl);
    post.setCreateTime(System.currentTimeMillis());
    post.setUpdateTime(System.currentTimeMillis());
    post.setVideoDuration(0);
    return post;
  }

  /**
   * 创建视频Post对象
   */
  public Post createVideoPost(String title, String content, String videoUrl,
                              String coverUrl, long duration, int userId) {
    Post post = new Post();
    post.setUserId(userId);
    post.setTitle(title);
    post.setContent(content);
    post.setVideoUrl(videoUrl);
    post.setVideo(true);
    post.setCoverUrl(coverUrl);
    post.setVideoDuration(duration);
    post.setCreateTime(System.currentTimeMillis());
    post.setUpdateTime(System.currentTimeMillis());
    return post;
  }

  /**
   * 创建PostImage列表
   */
  public List<PostImage> createPostImages(List<String> imageUrls) {
    List<PostImage> postImages = new ArrayList<>();
    for (int i = 0; i < imageUrls.size(); i++) {
      // postId暂时设为0，插入时会设置正确的值
      PostImage postImage = new PostImage(0, imageUrls.get(i), i);
      postImages.add(postImage);
    }
    return postImages;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (executor != null && !executor.isShutdown()) {
      executor.shutdown();
    }
  }
}