# Audiobooks Challenge App

A modern Android application built with Jetpack Compose that allows users to browse and discover podcasts from the Listen Notes API. The app features a clean Material 3 design, efficient pagination, and smooth navigation between podcast lists and details.

## 📱 Features

### 🎧 Podcast Discovery
- **Browse Best Podcasts**: View a curated list of top podcasts from the Listen Notes API
- **Efficient Pagination**: Load podcasts in pages of 10 for optimal performance
- **Smooth Scrolling**: Seamless navigation through large podcast collections

### 📋 Podcast Details
- **Comprehensive Information**: View podcast title, publisher, description, and images
- **High-Quality Images**: Display both thumbnail and full-size podcast artwork
- **Favourite System**: Mark podcasts as favourites with persistent state

### 🎨 Modern UI/UX
- **Material 3 Design**: Latest Android design system with dynamic theming
- **Edge-to-Edge Design**: Modern Android experience with immersive UI
- **Responsive Navigation**: Dynamic top bar that adapts to current screen
- **Loading States**: Clear feedback during data loading and pagination
- **Error Handling**: Graceful error messages with retry functionality

### 🔄 Smart Navigation
- **Intuitive Back Navigation**: Easy navigation between list and detail views
- **RTL Support**: Auto-mirrored back icons for international users
- **State Preservation**: Maintains favourite status across navigation

## 🚀 How to Use the App

### Getting Started
1. **Launch the App**: Open the Audiobooks Challenge app on your Android device
2. **Wait for Loading**: The app will automatically fetch the latest podcast data
3. **Browse Podcasts**: Scroll through the list of best podcasts

### Navigating the App

#### 📋 Podcast List Screen
- **View Podcasts**: See a paginated list of podcasts with thumbnails and titles
- **Scroll to Load More**: Automatically loads more podcasts as you scroll
- **Tap to View Details**: Tap any podcast to see detailed information
- **Favourite Indicators**: Red "Favourited" text shows your saved podcasts

#### 📖 Podcast Detail Screen
- **View Full Information**: See complete podcast details including description
- **Large Artwork**: View high-quality podcast images
- **Toggle Favourite**: Tap the "Favourite" button to save/unsave podcasts
- **Back Navigation**: Use the back button or gesture to return to the list

### Error Handling
- **Network Issues**: If data fails to load, tap "Retry" to try again
- **Missing Podcasts**: Clear error messages if a podcast cannot be found
- **Loading Feedback**: Spinning indicators show when data is being fetched

## 🛠 Technical Architecture

### Modern Android Development
- **Jetpack Compose**: Modern declarative UI toolkit
- **MVVM Architecture**: Clean separation of concerns with ViewModel
- **Kotlin Coroutines**: Asynchronous programming for smooth performance
- **StateFlow**: Reactive state management for UI updates

### Key Components

#### 📊 Data Layer
- **Podcast Data Class**: Immutable data model with serialization support
- **API Integration**: Listen Notes API for podcast data
- **Error Handling**: Comprehensive error states and retry mechanisms

#### 🎯 Business Logic
- **PodcastViewModel**: Central state management and business logic
- **Paging 3**: Efficient list pagination for large datasets
- **Favourite Management**: Persistent favourite state across app sessions

#### 🎨 UI Layer
- **Compose Navigation**: Type-safe navigation between screens
- **Material 3**: Latest Android design system components
- **Responsive Layout**: Adapts to different screen sizes and orientations

### Performance Optimizations
- **Pagination**: Loads data in chunks to minimize memory usage
- **Image Caching**: Efficient image loading with Coil library
- **State Caching**: ViewModel preserves state during configuration changes
- **Background Processing**: Network calls on background threads

## 📋 Requirements

### For Users
- **Android 6.0 (API 23)** or higher
- **Internet Connection** for podcast data
- **Minimum 2GB RAM** recommended for smooth performance

### For Developers
- **Android Studio Arctic Fox** or later
- **Kotlin 1.8+**
- **Android Gradle Plugin 8.0+**
- **JDK 17**

## 🔧 Setup Instructions

### For Users
1. **Download**: Install the APK file on your Android device
2. **Permissions**: Grant internet permission when prompted
3. **Launch**: Open the app and start browsing podcasts

### For Developers

#### Prerequisites
```bash
# Ensure you have the latest Android Studio
# Install JDK 17
# Clone the repository
git clone <repository-url>
cd AudiobooksChallenge
```

#### Build and Run
```bash
# Open in Android Studio
# Sync Gradle files
# Build the project
./gradlew build

# Run on device/emulator
./gradlew installDebug
```

#### Key Dependencies
```kotlin
// Core Android
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

// Compose
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")

// Paging
implementation("androidx.paging:paging-compose:3.2.1")

// Networking
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// Image Loading
implementation("io.coil-kt:coil-compose:2.5.0")

// Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
```

## 🏗 Project Structure

```
AudiobooksChallenge/
├── app/
│   └── src/main/java/com/hohar/audiobookschallenge/
│       ├── MainActivity.kt              # Main UI and navigation
│       ├── PodcastViewModel.kt          # Business logic and state management
│       ├── Podcast.kt                   # Data model
│       └── PodcastPagingSource.kt       # Pagination logic
├── build.gradle.kts                     # App-level build configuration
└── README.md                           # This file
```

## 🎯 Key Features Explained

### Pagination System
The app uses Android's Paging 3 library to efficiently handle large lists of podcasts:
- **Page Size**: 10 podcasts per page
- **Prefetch Distance**: 1 page ahead for smooth scrolling
- **Memory Efficient**: Only loads visible and nearby items
- **Automatic Loading**: Loads more content as user scrolls

### State Management
The app follows MVVM architecture with reactive state management:
- **Single Source of Truth**: ViewModel manages all app state
- **Reactive Updates**: UI automatically updates when data changes
- **Lifecycle Aware**: State survives configuration changes
- **Error Handling**: Comprehensive error states with retry functionality

### Navigation
Compose Navigation provides type-safe navigation:
- **Route-Based**: Clear navigation structure with defined routes
- **Parameter Passing**: Podcast IDs passed through navigation
- **Back Stack Management**: Proper back navigation handling
- **Dynamic Top Bar**: Context-aware app bar that changes per screen

## 🐛 Troubleshooting

### Common Issues

#### App Won't Load Podcasts
- **Check Internet**: Ensure you have a stable internet connection
- **Retry**: Tap the "Retry" button on the error screen
- **Restart App**: Close and reopen the app

#### Slow Performance
- **Close Other Apps**: Free up device memory
- **Check Storage**: Ensure sufficient storage space
- **Update App**: Install the latest version

#### Images Not Loading
- **Network Issues**: Check your internet connection
- **Storage Permission**: Ensure app has storage access
- **Clear Cache**: Clear app cache in device settings

### For Developers

#### Build Issues
```bash
# Clean and rebuild
./gradlew clean build

# Invalidate caches in Android Studio
File > Invalidate Caches / Restart
```

#### Runtime Issues
- **Check Logcat**: View detailed error logs in Android Studio
- **API Limits**: Ensure Listen Notes API is accessible
- **Device Compatibility**: Test on different Android versions

## 🤝 Contributing

### Development Guidelines
- **Kotlin Style**: Follow official Kotlin coding conventions
- **Compose Best Practices**: Use recommended Compose patterns
- **Testing**: Add unit tests for ViewModel and data classes
- **Documentation**: Update comments and README for changes

### Code Quality
- **Immutability**: Use immutable data structures where possible
- **Error Handling**: Implement comprehensive error handling
- **Performance**: Optimize for smooth user experience
- **Accessibility**: Ensure app is accessible to all users

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- **Listen Notes API**: For providing podcast data
- **Jetpack Compose**: Modern Android UI toolkit
- **Material Design**: Design system and components
- **Android Community**: For excellent documentation and examples

---

**Version**: 1.0.0  
**Last Updated**: December 2024  
**Maintainer**: [Your Name/Team] 