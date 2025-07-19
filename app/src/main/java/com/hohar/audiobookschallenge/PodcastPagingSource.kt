package com.hohar.audiobookschallenge

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * PagingSource implementation for podcast data pagination.
 * 
 * This class implements the Paging 3 library's PagingSource to provide
 * efficient pagination for the podcast list. It takes a static list of
 * podcasts and divides it into pages of 10 items each for optimal
 * performance and memory usage.
 * 
 * Key Features:
 * - Pages podcasts in groups of 10 items
 * - Handles edge cases (empty lists, partial pages)
 * - Provides proper navigation keys for forward/backward pagination
 * - Includes error handling for robust operation
 * - Optimized for small to medium-sized datasets
 * 
 * Usage:
 * This PagingSource is typically used with a Pager in the ViewModel
 * to create a Flow<PagingData<Podcast>> that can be consumed by
 * Compose's LazyColumn with collectAsLazyPagingItems().
 * 
 * @param allPodcasts The complete list of podcasts to be paginated
 */
class PodcastPagingSource(
    private val allPodcasts: List<Podcast>
) : PagingSource<Int, Podcast>() {
    
    /**
     * Loads a page of podcast data based on the provided parameters.
     * 
     * This method implements the core pagination logic:
     * 1. Calculates the page number from the load parameters
     * 2. Determines the start and end indices for the requested page
     * 3. Extracts the appropriate subset of podcasts
     * 4. Returns a LoadResult.Page with the data and navigation keys
     * 
     * Pagination Logic:
     * - Page size is fixed at 10 items per page
     * - Page 0 contains items 0-9, Page 1 contains items 10-19, etc.
     * - The last page may contain fewer than 10 items
     * - Navigation keys enable seamless forward/backward scrolling
     * 
     * Error Handling:
     * - Wraps the entire operation in a try-catch block
     * - Returns LoadResult.Error for any exceptions
     * - Ensures the UI can handle pagination failures gracefully
     * 
     * @param params Contains the page key and load size information
     * @return LoadResult.Page with the requested podcast data and navigation keys
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Podcast> {
        return try {
            // Extract the page number from the load parameters
            // If no key is provided, start from page 0
            val page = params.key ?: 0
            
            // Calculate the start index for this page
            // Each page contains 10 items, so page 0 = items 0-9, page 1 = items 10-19, etc.
            val fromIndex = page * 10
            
            // Calculate the end index, ensuring we don't exceed the list size
            // minOf ensures we don't go beyond the available data
            val toIndex = minOf(fromIndex + 10, allPodcasts.size)
            
            // Extract the podcast data for this page
            // If fromIndex is beyond the list size, return an empty list
            val data = if (fromIndex < allPodcasts.size) allPodcasts.subList(fromIndex, toIndex) else emptyList()
            
            // Create and return the page result with navigation keys
            LoadResult.Page(
                data = data,
                // Previous key: null for page 0, otherwise page - 1
                prevKey = if (page == 0) null else page - 1,
                // Next key: null if this is the last page, otherwise page + 1
                nextKey = if (toIndex < allPodcasts.size) page + 1 else null
            )
        } catch (e: Exception) {
            // Return an error result if any exception occurs
            // This ensures the UI can handle pagination failures gracefully
            LoadResult.Error(e)
        }
    }

    /**
     * Provides a refresh key for the current paging state.
     * 
     * This method is called by the Paging library when it needs to
     * refresh the data (e.g., after a configuration change or
     * when the user pulls to refresh).
     * 
     * Implementation:
     * - Returns 0 to always refresh from the beginning
     * - This is appropriate for static data that doesn't change
     * - For dynamic data, you might return a more sophisticated key
     * 
     * @param state The current paging state containing information about loaded pages
     * @return The refresh key (always 0 for this implementation)
     */
    override fun getRefreshKey(state: PagingState<Int, Podcast>): Int? = 0
}