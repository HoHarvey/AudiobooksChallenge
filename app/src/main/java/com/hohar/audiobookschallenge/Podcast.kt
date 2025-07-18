package com.hohar.audiobookschallenge

import kotlinx.serialization.Serializable

@Serializable
data class Podcast (
    var title: String,
    var publisherName: String,
    var thumbnail: String,
    var image: String,
    var description: String,
    var favorite: Boolean = false  // favorite not in json, set by user
)