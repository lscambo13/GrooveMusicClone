package com.msc24x.player

import android.net.Uri

// Need the READ_EXTERNAL_STORAGE permission if accessing video files that your
// app didn't create.

// Container for information about each video.
data class Songs(
    val uri: Uri?,
    val SongTitle: String,
    val ArtistName: String,
    val Duration: Int
)



