package com.msc24x.player

import android.net.Uri

data class Songs(
    var uri: Uri?,
    var SongTitle: String,
    var ArtistName: String,
    var AlbumName: String,
    var Duration: Int
)



