package com.example.rednotedemo.data.paging

import android.widget.Toast
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.rednotedemo.entity.vo.PostListItemVO
import com.example.rednotedemo.enums.FilterType
import kotlin.random.Random

class PostPagingSource(private val filterType: FilterType) : PagingSource<Int, PostListItemVO>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostListItemVO> {
        try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            // 模拟数据
            val items = ArrayList<PostListItemVO>()
            for (i in 1..pageSize) {
                val id = Random.nextInt(100000) + page * pageSize + i
                items.add(
                    PostListItemVO()
                        .setPostId(id)
                        .setCoverUrl("https://picsum.photos/300/400?random=$id")
                        .setTitle("动态 $id")
                        .setAuthorName("用户$id")
                        .setLikesCount(10 + id % 100)
                        .setType(filterType)
                )
            }
            return LoadResult.Page(
                data = items,
                prevKey = if (page == 0) null else page - 1,
                nextKey = page + 1
            )

        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PostListItemVO>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}