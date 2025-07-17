package com.hohar.audiobookschallenge

import java.net.URL

class PodcastItem {
    var title = ""
    var publisherName = ""
    lateinit var thumbnail: URL
    lateinit var image: URL
    var favorite = false
    var description = ""

    class PodcastItem constructor(title: String, publisherName: String, thumbnail: URL, image: URL,
                                  description: String, favorite: Boolean)
}