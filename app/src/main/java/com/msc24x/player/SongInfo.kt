package com.msc24x.player

import android.net.Uri

class SongInfo {
    var title: String? = null
    var artist: String? = null
    var link: Uri? = null

    constructor(title: String?, artist: String?, link: Uri?) {
        this.title = title
        this.artist = artist
        this.link = link
    }
}