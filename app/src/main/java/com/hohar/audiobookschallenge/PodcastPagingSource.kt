package com.hohar.audiobookschallenge

import androidx.paging.PagingSource
import androidx.paging.PagingState


class PodcastPagingSource(
    private val allPodcasts: List<Podcast>
) : PagingSource<Int, Podcast>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Podcast> {
        return try {
            val page = params.key ?: 0
            val fromIndex = page * 10
            val toIndex = minOf(fromIndex + 10, allPodcasts.size)
            val data = if (fromIndex < allPodcasts.size) allPodcasts.subList(fromIndex, toIndex) else emptyList()
            
            LoadResult.Page(
                data = data,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (toIndex < allPodcasts.size) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Podcast>): Int? = 0
}