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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.hohar.audiobookschallenge.ui.theme.AudiobooksChallengeTheme
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import androidx.paging.LoadState
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AudiobooksChallengeTheme {
                val navController = rememberNavController()
                val viewModel: PodcastViewModel = viewModel()
                MainScreen(navController = navController) {
                    NavHost(navController = navController, startDestination = "podcastList") {
                        composable("podcastList") {
                            val uiState by viewModel.uiState.collectAsState()
                            when (uiState) {
                                is UiState.Loading -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator()
                                        Text(
                                            text = "Loading podcasts...",
                                            modifier = Modifier.padding(top = 16.dp)
                                        )
                                    }
                                }
                                is UiState.Success -> {
                                    val lazyPagingItems = viewModel.podcastPagingData.collectAsLazyPagingItems()
                                    PodcastPagingList(
                                        lazyPagingItems = lazyPagingItems,
                                        onPodcastClick = { selectedPodcast ->
                                            navController.navigate("podcastDetail/${selectedPodcast.id}")
                                        }
                                    )
                                }
                                is UiState.Error -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = (uiState as UiState.Error).message,
                                            color = Color.Red,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        Button(
                                            onClick = { viewModel.retry() },
                                            modifier = Modifier.padding(top = 16.dp)
                                        ) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }
                        composable("podcastDetail/{podcastId}") { backStackEntry ->
                            val podcastId = backStackEntry.arguments?.getString("podcastId")
                            val podcast = viewModel.podcastList.collectAsState().value.find { it.id == podcastId }
                            if (podcast != null) {
                                PodcastDetails(
                                    podcast = podcast,
                                    onFavouriteClick = { viewModel.toggleFavourite(podcast.id) }
                                )
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
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
    fun PodcastPagingList(lazyPagingItems: LazyPagingItems<Podcast>, onPodcastClick: (Podcast) -> Unit) {
        LazyColumn {
            items(lazyPagingItems.itemCount) { index ->
                val podcast = lazyPagingItems[index]
                if (podcast != null) {
                    PodcastItem(podcast, onClick = { onPodcastClick(podcast) })
                }
            }
            if (lazyPagingItems.itemCount == 0) {
                item { Text("No podcasts found or still loading...") }
            }
            // Show spinner when loading next page
            if (lazyPagingItems.loadState.append is LoadState.Loading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    @Composable
    fun PodcastItem(podcast: Podcast, onClick: () -> Unit) {
        Column {
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
                    if (podcast.favourite) {
                        Text(
                            text = "Favourited",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.3f)
            )
        }
    }

    @Composable
    fun PodcastDetails(
        podcast: Podcast,
        onFavouriteClick: () -> Unit
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
            if (!podcast.image.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(podcast.image),
                    contentDescription = podcast.title ?: "",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

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
                    text = if (podcast.favourite) "Favourited" else "Favourite",
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
}






