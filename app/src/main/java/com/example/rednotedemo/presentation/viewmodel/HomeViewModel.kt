package com.example.rednotedemo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.rednotedemo.data.paging.PostPagingSource
import com.example.rednotedemo.entity.vo.PostListItemVO
import com.example.rednotedemo.enums.FilterType
import kotlinx.coroutines.flow.Flow

class HomeViewModel(
    private val filterType: FilterType = FilterType.DISCOVER // 保留参数，默认 DISCOVER
) : ViewModel() {

    private var _postsFlow: Flow<PagingData<PostListItemVO>>? = null

    fun getPosts(): Flow<PagingData<PostListItemVO>> {
//        Thread.sleep(1000)
        if (_postsFlow == null) {
            _postsFlow = Pager(
                config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                pagingSourceFactory = { PostPagingSource(filterType) } // 传入 filterType
            ).flow.cachedIn(viewModelScope)
        }
        return _postsFlow!!
    }

    fun refresh() {
        _postsFlow = null // 重建 Flow 实现刷新
    }
}