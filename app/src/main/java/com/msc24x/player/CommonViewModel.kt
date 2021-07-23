package com.msc24x.player

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msc24x.player.data.database.Track


class CommonViewModel : ViewModel() {

    var trackPlaylist = mutableListOf<Track>()
    var currentTrack: Track? = null

    val currentSong = MutableLiveData<String>()
    val currentArtist = MutableLiveData<String>()
    val currentPosition = MutableLiveData<Int>()
    val currentUri = MutableLiveData<Uri>()
    val busy = MutableLiveData<Boolean>()
    val songLength = MutableLiveData<Int>()
    val mutedColor = MutableLiveData<Int>()
    val decodedArt = MutableLiveData<Bitmap>()
}