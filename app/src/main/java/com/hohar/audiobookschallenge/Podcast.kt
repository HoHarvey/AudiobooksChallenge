package com.hohar.audiobookschallenge

import kotlinx.serialization.Serializable

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