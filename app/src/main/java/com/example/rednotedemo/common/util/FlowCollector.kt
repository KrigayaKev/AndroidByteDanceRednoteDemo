package com.example.rednotedemo.common.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.example.rednotedemo.entity.vo.PostListItemVO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun collectPagingData(
    owner: LifecycleOwner,
    flow: Flow<PagingData<PostListItemVO>>,
    onCollect: (PagingData<PostListItemVO>) -> Unit
): Unit {
    owner.lifecycleScope.launch {
        flow.collect { pagingData ->
            onCollect(pagingData)
        }
    }
}