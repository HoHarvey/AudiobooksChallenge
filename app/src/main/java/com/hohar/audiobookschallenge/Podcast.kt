package com.hohar.audiobookschallenge

import kotlinx.serialization.Serializable

/**
 * Data class representing a podcast entity in the application.
 * 
 * This class serves as the primary data model for podcast information,
 * designed to work with the Listen Notes API and provide a clean interface
 * for the UI layer. It uses Kotlinx Serialization for JSON parsing.
 * 
 * Design Decisions:
 * - All fields except 'id' are nullable to handle incomplete API responses gracefully
 * - 'id' is non-nullable as it's required for navigation and data identification
 * - 'favourite' is immutable (val) to enforce data integrity through ViewModel updates
 * - Default values are provided for optional fields to ensure safe usage
 * 
 * @property id Unique identifier for the podcast (required for navigation and data operations)
 * @property title The name/title of the podcast (nullable for API compatibility)
 * @property publisherName The name of the podcast publisher/creator (nullable for API compatibility)
 * @property image High-resolution image URL for the podcast (nullable for API compatibility)
 * @property thumbnail Low-resolution thumbnail URL for the podcast (nullable for API compatibility)
 * @property description Detailed description of the podcast content (nullable for API compatibility)
 * @property favourite User's favourite status for this podcast (defaults to false)
 */
@Serializable
data class Podcast(
    val id: String,
    val title: String? = null,
    val publisherName: String? = null,
    val image: String? = null,
    val thumbnail: String? = null,
    val description: String? = null,
    val favourite: Boolean = false
)