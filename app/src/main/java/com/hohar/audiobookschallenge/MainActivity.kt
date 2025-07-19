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

/**
 * Main Activity for the Audiobooks Challenge application.
 * 
 * This activity serves as the entry point for the app and implements
 * a modern Android application using Jetpack Compose for the UI layer.
 * It follows the MVVM architecture pattern with a ViewModel for state
 * management and Compose Navigation for screen transitions.
 * 
 * Key Features:
 * - Jetpack Compose UI with Material 3 design system
 * - Navigation between podcast list and detail screens
 * - Paging 3 integration for efficient list handling
 * - State management through ViewModel
 * - Error handling with retry functionality
 * - Edge-to-edge design for modern Android experience
 * 
 * Architecture:
 * - Uses ComponentActivity as the base class
 * - Implements Compose Navigation with NavHost
 * - Observes ViewModel state through StateFlow
 * - Handles UI state changes (loading, success, error)
 * - Provides responsive navigation with dynamic top bar
 */
class MainActivity : ComponentActivity() {
    
    /**
     * Main entry point for the activity.
     * 
     * This method sets up the Compose UI and initializes the navigation
     * system. It enables edge-to-edge design and configures the theme
     * and navigation structure.
     * 
     * Setup Process:
     * 1. Enables edge-to-edge design for modern Android experience
     * 2. Sets up Compose content with the app theme
     * 3. Initializes navigation controller and ViewModel
     * 4. Configures NavHost with route definitions
     * 5. Handles different UI states (loading, success, error)
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge design for modern Android experience
        enableEdgeToEdge()
        setContent {
            // Apply the app theme to all composables
            AudiobooksChallengeTheme {
                // Initialize navigation controller for screen transitions
                val navController = rememberNavController()
                // Get ViewModel instance for state management
                val viewModel: PodcastViewModel = viewModel()
                
                // Main screen container with dynamic top bar
                MainScreen(navController = navController) {
                    // Navigation host with route definitions
                    NavHost(navController = navController, startDestination = "podcastList") {
                        
                        // Podcast list screen route
                        composable("podcastList") {
                            // Observe UI state from ViewModel
                            val uiState by viewModel.uiState.collectAsState()
                            
                            // Handle different UI states
                            when (uiState) {
                                // Show loading indicator while fetching data
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
                                
                                // Show paginated podcast list when data is loaded
                                is UiState.Success -> {
                                    // Collect paging data for efficient list display
                                    val lazyPagingItems = viewModel.podcastPagingData.collectAsLazyPagingItems()
                                    PodcastPagingList(
                                        lazyPagingItems = lazyPagingItems,
                                        onPodcastClick = { selectedPodcast ->
                                            // Navigate to detail screen with podcast ID
                                            navController.navigate("podcastDetail/${selectedPodcast.id}")
                                        }
                                    )
                                }
                                
                                // Show error message with retry option
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
                        
                        // Podcast detail screen route with dynamic parameter
                        composable("podcastDetail/{podcastId}") { backStackEntry ->
                            // Extract podcast ID from navigation arguments
                            val podcastId = backStackEntry.arguments?.getString("podcastId")
                            // Find the podcast in the current list
                            val podcast = viewModel.podcastList.collectAsState().value.find { it.id == podcastId }
                            
                            if (podcast != null) {
                                // Show podcast details with favourite toggle functionality
                                PodcastDetails(
                                    podcast = podcast,
                                    onFavouriteClick = { viewModel.toggleFavourite(podcast.id) }
                                )
                            } else {
                                // Show error message if podcast not found
                                Text("Podcast not found")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Main screen container with dynamic top bar.
     * 
     * This composable provides the main layout structure with a Scaffold
     * that includes a dynamic top bar that changes based on the current
     * navigation route. It handles the transition between the podcast
     * list view and the detail view.
     * 
     * Top Bar Behavior:
     * - Podcast List: Shows "Podcasts" title
     * - Podcast Detail: Shows back button with "Back" label
     * 
     * @param navController Navigation controller for handling back navigation
     * @param content Composable content to be displayed in the main area
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(navController: NavHostController, content: @Composable () -> Unit) {
        // Get current navigation state to determine top bar content
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            topBar = {
                // Dynamic top bar based on current route
                when {
                    // Show "Podcasts" title for the list screen
                    currentRoute == "podcastList" -> {
                        TopAppBar(
                            title = { Text("Podcasts") }
                        )
                    }
                    // Show back button for detail screens
                    currentRoute?.startsWith("podcastDetail") == true -> {
                        TopAppBar(
                            navigationIcon = {
                                // Custom back button with icon and text
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { navController.popBackStack() }
                                        .padding(start = 8.dp)
                                ) {
                                    // Auto-mirrored back icon for RTL support
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
                            title = { } // Empty title for detail screens
                        )
                    }
                }
            }
        ) { innerPadding ->
            // Apply padding from top bar to content
            Column(modifier = Modifier.padding(innerPadding)) {
                content()
            }
        }
    }

    /**
     * Paginated list of podcasts using Paging 3.
     * 
     * This composable displays a scrollable list of podcasts with
     * pagination support. It shows a loading indicator when loading
     * additional pages and handles empty states gracefully.
     * 
     * Features:
     * - Efficient pagination with Paging 3
     * - Loading indicators for next page
     * - Empty state handling
     * - Click handling for navigation to detail screen
     * 
     * @param lazyPagingItems Paging items from the ViewModel
     * @param onPodcastClick Callback for podcast item clicks
     */
    @Composable
    fun PodcastPagingList(lazyPagingItems: LazyPagingItems<Podcast>, onPodcastClick: (Podcast) -> Unit) {
        LazyColumn {
            // Display podcast items
            items(lazyPagingItems.itemCount) { index ->
                val podcast = lazyPagingItems[index]
                if (podcast != null) {
                    PodcastItem(podcast, onClick = { onPodcastClick(podcast) })
                }
            }
            
            // Show message when no podcasts are available
            if (lazyPagingItems.itemCount == 0) {
                item { Text("No podcasts found or still loading...") }
            }
            
            // Show loading spinner when loading next page
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

    /**
     * Individual podcast item in the list.
     * 
     * This composable displays a single podcast with its thumbnail,
     * title, publisher name, and favourite status. It includes a
     * subtle divider line and handles click events for navigation.
     * 
     * Layout:
     * - Horizontal layout with thumbnail on the left
     * - Text information on the right
     * - Faint divider line at the bottom
     * - Rounded corners for thumbnail
     * 
     * @param podcast Podcast data to display
     * @param onClick Callback for item click events
     */
    @Composable
    fun PodcastItem(podcast: Podcast, onClick: () -> Unit) {
        Column {
            // Main content row with thumbnail and text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(8.dp)
            ) {
                // Podcast thumbnail with null safety
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
                
                // Text information column
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    // Podcast title with ellipsis for overflow
                    Text(
                        text = podcast.title ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Publisher name in italic gray text
                    Text(
                        text = podcast.publisherName ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )
                    
                    // Favourite indicator in red text
                    if (podcast.favourite) {
                        Text(
                            text = "Favourited",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }
            
            // Subtle divider line
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.3f)
            )
        }
    }

    /**
     * Detailed view of a single podcast.
     * 
     * This composable displays comprehensive information about a podcast
     * including its title, publisher, image, description, and favourite
     * status. It provides a scrollable layout with a favourite toggle
     * button and handles image loading with null safety.
     * 
     * Layout:
     * - Centered column layout
     * - Large podcast image
     * - Favourite toggle button
     * - Scrollable description
     * - Proper spacing and typography
     * 
     * @param podcast Podcast data to display in detail
     * @param onFavouriteClick Callback for favourite button clicks
     */
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
            // Podcast title with bold styling
            Text(
                text = podcast.title ?: "",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            // Publisher name in italic gray text
            Text(
                text = podcast.publisherName ?: "",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Large podcast image with null safety
            if (!podcast.image.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(podcast.image),
                    contentDescription = podcast.title ?: "",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            // Favourite toggle button with custom styling
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

            // Podcast description in centered gray text
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






