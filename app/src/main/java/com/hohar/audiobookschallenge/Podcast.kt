package com.hohar.audiobookschallenge

import kotlinx.serialization.Serializable

@Serializable
data class Podcast(
    val id: String? = null,
    val title: String? = null,
    val publisherName: String? = null,
    val image: String? = null,
    val thumbnail: String? = null,
    val description: String? = null,
    val favorite: Boolean = false
)