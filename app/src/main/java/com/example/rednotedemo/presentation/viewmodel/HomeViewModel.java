package com.example.rednotedemo.presentation.viewmodel;

import com.example.rednotedemo.data.paging.PostPagingSource;
import com.example.rednotedemo.data.repository.PostRepository;
import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.common.enums.FilterType;
import com.example.rednotedemo.common.util.Resource;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava2.PagingRx;

import io.reactivex.Flowable;
import kotlinx.coroutines.CoroutineScope;

public class HomeViewModel extends ViewModel {
  private final PostRepository postRepository;
  private MutableLiveData<Resource<List<PostListItemVO>>> postsLiveData = new MutableLiveData<>();

  public HomeViewModel() {
    postRepository = new PostRepository();
    postsLiveData = new MutableLiveData<>();
  }

    public Flowable<PagingData<PostListItemVO>> getPagingData(FilterType type) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);

        Pager<Integer, PostListItemVO> pager = new Pager<>(
                new PagingConfig(
                        10,        // pageSize
                        10,        // prefetchDistance
                        true,      // enablePlaceholders
                        10         // initialLoadSize
                ),
                () -> new PostPagingSource(type, postRepository)
        );

        Flowable<PagingData<PostListItemVO>> flowable = PagingRx.getFlowable(pager);
        return PagingRx.cachedIn(flowable, viewModelScope); // 缓存，避免旋转重建重复请求
    }

  public LiveData<Resource<List<PostListItemVO>>> getPosts() {
    return postsLiveData;
  }

}