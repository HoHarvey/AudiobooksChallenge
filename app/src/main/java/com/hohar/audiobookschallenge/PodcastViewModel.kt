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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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

class PodcastViewModel : ViewModel() {
    private val client = OkHttpClient()
    private val _podcastList = MutableStateFlow<List<Podcast>>(emptyList())
    val podcastList: StateFlow<List<Podcast>> = _podcastList.asStateFlow()

    // PagingData flow for Compose
    val podcastPagingData: Flow<PagingData<Podcast>> = podcastList
        .flatMapLatest { list ->
            Pager(PagingConfig(pageSize = 10)) {
                PodcastPagingSource(list)
            }.flow
        }
        .cachedIn(viewModelScope)

    init {
        fetchBestPodcasts()
    }

    fun fetchBestPodcasts() {
        viewModelScope.launch {
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
            } else {
                println("HTTP error: ${response.code}")
            }
        }
    }
}