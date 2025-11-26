package com.example.rednotedemo.presentation.viewmodel;

import android.os.Handler;
import android.os.Looper;

import com.example.rednotedemo.data.repository.PostRepository;
import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.util.Resource;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
  private final PostRepository postRepository;
  private MutableLiveData<Resource<List<PostListItemVO>>> postsLiveData = new MutableLiveData<>();

  public HomeViewModel() {
    postRepository = new PostRepository();
    postsLiveData = new MutableLiveData<>();
  }

  public LiveData<Resource<List<PostListItemVO>>> getPosts() {
    return postsLiveData;
  }

  public void loadPosts() {
    postsLiveData.setValue(Resource.loading()); // 显示加载中

    // 模拟延迟（真实场景可能是网络请求）
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      List<PostListItemVO> data = createDummyData();
      postsLiveData.setValue(Resource.success(data));
    }, 1000);
  }

  private List<PostListItemVO> createDummyData() {
    postRepository.loadPosts();
    return postRepository.getPosts();
  }
}