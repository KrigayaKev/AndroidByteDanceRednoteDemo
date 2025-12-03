package com.example.rednotedemo.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rednotedemo.entity.Comment;
import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.PostImage;
import com.example.rednotedemo.entity.User;
import com.example.rednotedemo.data.repository.PostRepository;

import java.util.List;

public class PostDetailViewModel extends AndroidViewModel {
    private final PostRepository repository;
    private final MutableLiveData<Integer> postId = new MutableLiveData<>();

    private final MediatorLiveData<Post> post = new MediatorLiveData<>();
    private final MediatorLiveData<List<PostImage>> postImages = new MediatorLiveData<>();
    private final MediatorLiveData<List<Comment>> comments = new MediatorLiveData<>();

    public PostDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new PostRepository(application);

        // 设置帖子数据源
        post.addSource(postId, id -> {
            if (id != null) {
                post.addSource(repository.getPostById(id), post::setValue);
            }
        });

        // 设置图片数据源
        postImages.addSource(postId, id -> {
            if (id != null) {
                postImages.addSource(repository.getPostImages(id), postImages::setValue);
            }
        });

        // 设置评论数据源
        comments.addSource(postId, id -> {
            if (id != null) {
                comments.addSource(repository.getCommentsByPostId(id), comments::setValue);
            }
        });
    }

    public void setPostId(int id) {
        postId.setValue(id);
    }

    public LiveData<Post> getPost() {
        return post;
    }

    public LiveData<List<PostImage>> getPostImages() {
        return postImages;
    }

    public LiveData<User> getAuthor(int userId) {
        return repository.getUserById(userId);
    }

    public LiveData<List<Comment>> getComments() {
        return comments;
    }

    public void addComment(String content, int userId) {
        if (postId.getValue() != null) {
            Comment comment = new Comment(postId.getValue(), userId, content);
            repository.insertComment(comment);
        }
    }

    public void deleteComment(int commentId) {
        repository.deleteComment(commentId);
    }
}