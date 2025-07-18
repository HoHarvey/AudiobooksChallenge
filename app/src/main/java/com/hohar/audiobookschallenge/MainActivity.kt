package com.hohar.audiobookschallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.hohar.audiobookschallenge.ui.theme.AudiobooksChallengeTheme
import com.hohar.audiobookschallenge.Podcast
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import androidx.compose.runtime.mutableStateListOf



class MainActivity : ComponentActivity() {
    private val client = OkHttpClient()
    private val podcastList = mutableStateListOf<Podcast>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchBestPodcasts()
        enableEdgeToEdge()
        setContent {
            AudiobooksChallengeTheme {
                Scaffold(
                    topBar = {
                        // add top bar with title of Podcasts
                        TopAppBar(
                            title = { Text("Podcasts") }
                        )
                    }
                ) { innerPadding ->
                    // Your main screen content goes here
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ){
                        if (podcastList.isEmpty()) {
                            Text("Loading podcasts...")
                        } else {
                            PodcastList(podcastList)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PodcastList(podcasts: List<Podcast>){
        Column {
            podcasts.forEach { podcast ->
                PodcastItem(podcast)
            }
        }
    }

    @Composable
    fun PodcastItem(podcast: Podcast) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (!podcast.thumbnail.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(podcast.thumbnail),
                    contentDescription = podcast.title ?: "",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = podcast.title ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = podcast.publisherName ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
                if (podcast.favorite ?: false){
                    Text(
                        text = "Favourited",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }
        }
    }

    private fun fetchBestPodcasts() {
        val request = Request.Builder()
            .url("https://listen-api-test.listennotes.com/api/v2/best_podcasts")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    println("Network error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            println("HTTP error: ${response.code}")
                        }
                        return
                    }

                    val responseBody = response.body?.string() ?: ""
                    runOnUiThread {
                        println("Raw JSON: $responseBody")
                        val json = Json { ignoreUnknownKeys = true }
                        val jsonElement = json.parseToJsonElement(responseBody)
                        val podcastsJsonArray = jsonElement.jsonObject["podcasts"]!!
                        println("Parsed podcasts: " + podcastsJsonArray.toString())
                        val parsedPodcasts = json.decodeFromJsonElement<List<Podcast>>(podcastsJsonArray)
                        println("Decoded podcasts: $parsedPodcasts")
                        podcastList.clear()
                        podcastList.addAll(parsedPodcasts)
                    }
                }
            }
        })
    }
}





