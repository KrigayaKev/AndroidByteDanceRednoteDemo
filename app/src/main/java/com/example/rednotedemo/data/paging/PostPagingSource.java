package com.example.rednotedemo.data.paging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava2.RxPagingSource;

import com.example.rednotedemo.data.repository.PostPageRepository;
import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.common.enums.FilterType;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class PostPagingSource extends RxPagingSource<Integer, PostListItemVO> {
    private PostPageRepository repository;
    private FilterType filterType;

    public PostPagingSource(FilterType filterType, PostPageRepository repository) {
        this.filterType = filterType;
        this.repository = repository;
    }

    @Override
    public @NotNull Single<LoadResult<Integer, PostListItemVO>> loadSingle(@NotNull LoadParams<Integer> loadParams) {
        Integer nextPageNumber = loadParams.getKey();
        if (nextPageNumber == null) nextPageNumber = 1;

        int loadSize = loadParams.getLoadSize(); // 每页条数

        Integer finalNextPageNumber = nextPageNumber;
        return Single.fromCallable(() ->
                        repository.getNeedPagingList(filterType, loadSize, loadSize * (finalNextPageNumber - 1))
                )
                .subscribeOn(Schedulers.io())
                .map(mBeans -> toLoadResult(mBeans, finalNextPageNumber))
                .onErrorReturn(throwable -> new LoadResult.Error<>(throwable));
    }

    private LoadResult<Integer, PostListItemVO> toLoadResult(@NonNull List<PostListItemVO> mBeans, Integer page) {
        Integer prevKey = (page == 1) ? null : page - 1;
        Integer nextKey = mBeans.isEmpty() ? null : page + 1;
        return new LoadResult.Page<>(
                mBeans,
                prevKey,
                nextKey,
                LoadResult.Page.COUNT_UNDEFINED,
                LoadResult.Page.COUNT_UNDEFINED
        );
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, PostListItemVO> pagingState) {
        return 1;
    }
}
