package com.hohar.audiobookschallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.hohar.audiobookschallenge.ui.theme.AudiobooksChallengeTheme
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


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
                val navController = rememberNavController()
                MainScreen(navController = navController) {
                    NavHost(navController = navController, startDestination = "podcastList") {
                        composable("podcastList") {
                            PodcastList(podcastList) { selectedPodcast ->
                                navController.navigate("podcastDetail/${selectedPodcast.id}")
                            }
                        }
                        composable("podcastDetail/{podcastId}") { backStackEntry ->
                            val podcastId = backStackEntry.arguments?.getString("podcastId")
                            val podcast = podcastList.find { it.id == podcastId }
                            if (podcast != null) {
                                PodcastDetails(podcast = podcast)
                            } else {
                                Text("Podcast not found")
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(navController: NavHostController, content: @Composable () -> Unit) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            topBar = {
                when {
                    currentRoute == "podcastList" -> {
                        TopAppBar(
                            title = { Text("Podcasts") }
                        )
                    }
                    currentRoute?.startsWith("podcastDetail") == true -> {
                        TopAppBar(
                            navigationIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { navController.popBackStack() }
                                        .padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                    Text(
                                        text = "Back",
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            },
                            title = { }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                content()
            }
        }
    }
    @Composable
    fun PodcastList(podcasts: List<Podcast>, onPodcastClick: (Podcast) -> Unit) {
        Column {
            podcasts.forEach { podcast ->
                PodcastItem(podcast, onClick = { onPodcastClick(podcast) })
            }
        }
    }

    @Composable
    fun PodcastItem(podcast: Podcast, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
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
                if (podcast.favourite ?: false){
                    Text(
                        text = "Favourited",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }
        }
    }

    @Composable
    fun PodcastDetails(
        podcast: Podcast,
        onFavouriteClick: () -> Unit = {
            // if favorite is true, make it false and vice versa
            podcast.favourite = !podcast.favourite
        },
        //set ifFavourite to podcast favourite property
        isFavourite: Boolean = podcast.favourite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title and publisher
            Text(
                text = podcast.title ?: "",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = podcast.publisherName ?: "",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Podcast image
            Image(
                painter = rememberAsyncImagePainter(podcast.image),
                contentDescription = podcast.title ?: "",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            // Favourite button
            Button(
                onClick = onFavouriteClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B4B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .fillMaxWidth(0.5f)
            ) {
                Text(
                    text = if (isFavourite) "Favourited" else "Favourite",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Description
            Text(
                text = podcast.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
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





