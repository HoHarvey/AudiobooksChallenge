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
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

class PodcastViewModel : ViewModel() {
    private val client = OkHttpClient()
    private val _podcastList = MutableStateFlow<List<Podcast>>(emptyList())
    val podcastList: StateFlow<List<Podcast>> = _podcastList.asStateFlow()

    // PagingData flow for Compose
    val podcastPagingData: Flow<PagingData<Podcast>> = Pager(PagingConfig(pageSize = 10)) {
        PodcastPagingSource(_podcastList.value)
    }.flow.stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    init {
        fetchBestPodcasts()
    }

    fun fetchBestPodcasts() {
        viewModelScope.launch {
            val request = Request.Builder()
                .url("https://listen-api-test.listennotes.com/api/v2/best_podcasts")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = Json { ignoreUnknownKeys = true }
                val jsonElement = json.parseToJsonElement(responseBody)
                val podcastsJsonArray = jsonElement.jsonObject["podcasts"]!!
                val parsedPodcasts = json.decodeFromJsonElement<List<Podcast>>(podcastsJsonArray)
                _podcastList.value = parsedPodcasts
            }
        }
    }

    fun toggleFavourite(podcastId: String) {
        _podcastList.value = _podcastList.value.map {
            if (it.id == podcastId) it.copy(favourite = !it.favourite) else it
        }
    }
}