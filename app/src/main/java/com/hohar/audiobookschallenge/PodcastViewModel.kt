package com.hohar.audiobookschallenge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Sealed class representing the different states of the UI.
 * 
 * This class provides a type-safe way to handle different UI states
 * and ensures that all possible states are handled in the UI layer.
 * 
 * @property Loading Represents the initial loading state when fetching data
 * @property Success Represents the successful state with the fetched podcast list
 * @property Error Represents an error state with a descriptive error message
 */
sealed class UiState {
    object Loading : UiState()
    data class Success(val podcasts: List<Podcast>) : UiState()
    data class Error(val message: String) : UiState()
}

/**
 * ViewModel responsible for managing podcast data and business logic.
 * 
 * This ViewModel follows the MVVM architecture pattern and serves as the
 * single source of truth for podcast-related data in the application.
 * It handles network requests, data transformation, and state management
 * using Kotlin Coroutines and StateFlow for reactive programming.
 * 
 * Key Responsibilities:
 * - Fetching podcast data from the Listen Notes API
 * - Managing UI state (loading, success, error)
 * - Handling pagination for efficient list display
 * - Managing favourite status for podcasts
 * - Providing error handling and retry functionality
 * 
 * Architecture Features:
 * - Uses StateFlow for reactive state management
 * - Implements Paging 3 for efficient list handling
 * - Follows immutability principles for data updates
 * - Provides comprehensive error handling
 * - Uses coroutines for asynchronous operations
 */
class PodcastViewModel : ViewModel() {
    
    /**
     * HTTP client for making network requests to the Listen Notes API.
     * Configured as a private property to ensure proper lifecycle management.
     */
    private val client = OkHttpClient()
    
    /**
     * Mutable StateFlow containing the list of podcasts.
     * This is the internal state that should not be exposed directly to the UI.
     * Updates are made through ViewModel methods to maintain data integrity.
     */
    private val _podcastList = MutableStateFlow<List<Podcast>>(emptyList())
    
    /**
     * Public StateFlow exposing the podcast list to the UI.
     * This is the immutable version that the UI should observe.
     * Changes to this flow will automatically trigger UI recomposition.
     */
    val podcastList: StateFlow<List<Podcast>> = _podcastList.asStateFlow()
    
    /**
     * Mutable StateFlow containing the current UI state.
     * Manages loading, success, and error states for better user experience.
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    
    /**
     * Public StateFlow exposing the UI state to the UI layer.
     * Allows the UI to react to state changes and show appropriate content.
     */
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * PagingData flow for efficient list pagination in Compose.
     * 
     * This flow automatically handles pagination by:
     * - Creating a new Pager when the underlying podcast list changes
     * - Using flatMapLatest to ensure only the latest data is used
     * - Caching the flow in the ViewModel scope for lifecycle safety
     * - Configuring pagination with 10 items per page and prefetch distance of 1
     * 
     * The @OptIn annotation is required for the flatMapLatest operator.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val podcastPagingData: Flow<PagingData<Podcast>> = podcastList
        .flatMapLatest { list ->
            Pager(PagingConfig(pageSize = 10, prefetchDistance = 1)) {
                PodcastPagingSource(list)
            }.flow
        }
        .cachedIn(viewModelScope)

    /**
     * Initialization block that triggers the initial data fetch.
     * Called when the ViewModel is created, ensuring data is loaded immediately.
     */
    init {
        fetchBestPodcasts()
    }

    /**
     * Fetches the best podcasts from the Listen Notes API.
     * 
     * This method performs the following operations:
     * 1. Sets the UI state to Loading
     * 2. Makes an HTTP request to the API endpoint
     * 3. Parses the JSON response using Kotlinx Serialization
     * 4. Updates the podcast list and UI state accordingly
     * 5. Handles errors gracefully with appropriate error messages
     * 
     * The network request is executed on the IO dispatcher to avoid blocking
     * the main thread, and the response is processed on the main thread.
     */
    fun fetchBestPodcasts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val request = Request.Builder()
                    .url("https://listen-api-test.listennotes.com/api/v2/best_podcasts")
                    .build()
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val json = Json { ignoreUnknownKeys = true }
                    val jsonElement = json.parseToJsonElement(responseBody)
                    val podcastsJsonArray = jsonElement.jsonObject["podcasts"]!!
                    val parsedPodcasts = json.decodeFromJsonElement<List<Podcast>>(podcastsJsonArray)
                    println("Fetched podcasts: ${parsedPodcasts.size}")
                    _podcastList.value = parsedPodcasts
                    _uiState.value = UiState.Success(parsedPodcasts)
                } else {
                    _uiState.value = UiState.Error("HTTP error: ${response.code}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Network error: ${e.message}")
            }
        }
    }
    
    /**
     * Toggles the favourite status of a specific podcast.
     * 
     * This method follows immutability principles by:
     * 1. Creating a new list with the updated podcast
     * 2. Using the copy() method to create a new Podcast instance
     * 3. Preserving all other podcasts unchanged
     * 
     * The UI will automatically update when the StateFlow emits the new value.
     * 
     * @param podcastId The unique identifier of the podcast to toggle
     */
    fun toggleFavourite(podcastId: String) {
        _podcastList.value = _podcastList.value.map {
            if (it.id == podcastId) it.copy(favourite = !it.favourite) else it
        }
    }
    
    /**
     * Retries the data fetch operation.
     * 
     * This method provides a way for users to retry failed network requests
     * without having to restart the application. It simply calls fetchBestPodcasts()
     * to attempt the operation again.
     */
    fun retry() {
        fetchBestPodcasts()
    }
}