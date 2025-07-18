package com.hohar.audiobookschallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
    private var podcastList: ArrayList<Podcast> = arrayListOf()

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
                // Handle network error or request failure
                runOnUiThread {
                    // Update UI or show error message
                    println("Network error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { // Ensures response body is closed
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            // Handle HTTP error
                            println("HTTP error: ${response.code}")
                        }
                        return
                    }

                    val responseBody = response.body.string()
                    runOnUiThread {
                        // Process the JSON response
                        val jsonElement = Json.parseToJsonElement(responseBody)
                        val podcastsJsonArray = jsonElement.jsonObject["podcasts"]!!
                        podcastList = Json.decodeFromJsonElement<ArrayList<Podcast>>(
                            podcastsJsonArray)
                    }
                }
            }
        })
    }
}





