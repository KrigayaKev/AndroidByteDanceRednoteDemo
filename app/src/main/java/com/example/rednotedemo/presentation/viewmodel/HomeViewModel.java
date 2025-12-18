package com.example.rednotedemo.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava2.PagingRx;

import com.example.rednotedemo.data.paging.PostPagingSource;
import com.example.rednotedemo.data.repository.PostPageRepository;
import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.common.enums.FilterType;
import com.example.rednotedemo.common.util.Resource;

import java.util.List;

import io.reactivex.Flowable;
import kotlinx.coroutines.CoroutineScope;

public class HomeViewModel extends AndroidViewModel {
  private final PostPageRepository postRepository;
  private MutableLiveData<Resource<List<PostListItemVO>>> postsLiveData;
  private MutableLiveData<Boolean> isEmptyState = new MutableLiveData<>(false);
  private MutableLiveData<String> emptyStateMessage = new MutableLiveData<>("");
  private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

  public HomeViewModel(@NonNull Application application) {
    super(application);
    postRepository = new PostPageRepository(application);
    postsLiveData = new MutableLiveData<>();

    // 检查数据库状态
    checkDatabaseState();
  }

  public Flowable<PagingData<PostListItemVO>> getPagingData(FilterType type) {
    CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);

    Pager<Integer, PostListItemVO> pager = new Pager<>(
       new PagingConfig(
          10,        // pageSize
          5,         // prefetchDistance
          false,     // enablePlaceholders - 设为false避免占位符
          20         // initialLoadSize
       ),
       () -> new PostPagingSource(type, postRepository)
    );

    Flowable<PagingData<PostListItemVO>> flowable = PagingRx.getFlowable(pager);
    return PagingRx.cachedIn(flowable, viewModelScope);
  }

  public LiveData<Resource<List<PostListItemVO>>> getPosts() {
    return postsLiveData;
  }

  public LiveData<Boolean> getIsEmptyState() {
    return isEmptyState;
  }

  public LiveData<String> getEmptyStateMessage() {
    return emptyStateMessage;
  }

  public LiveData<Boolean> getIsLoading() {
    return isLoading;
  }

  // 检查数据库状态
  private void checkDatabaseState() {
    boolean isEmpty = postRepository.isDatabaseEmpty();
    isEmptyState.postValue(isEmpty);

    if (isEmpty) {
      emptyStateMessage.postValue("还没有帖子，快去发布第一个吧！");
    } else {
      emptyStateMessage.postValue("暂无帖子内容");
    }
  }

  // 刷新数据状态
  public void refreshDatabaseState() {
    checkDatabaseState();
  }

  // 设置加载状态
  public void setLoading(boolean loading) {
    isLoading.postValue(loading);
  }
}