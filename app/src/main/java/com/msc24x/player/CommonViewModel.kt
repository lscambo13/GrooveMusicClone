package com.msc24x.player

import android.graphics.Bitmap
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msc24x.player.data.database.Track
import com.msc24x.player.data.database.TrackDao


class CommonViewModel @ViewModelInject constructor(private val trackDao: TrackDao) : ViewModel() {

    //var trackPlaylist = mutableListOf<Track>()
    val currentTrack = MutableLiveData<Track>()

    val searchQuery = MutableLiveData<String>("")

    val currentPosition = MutableLiveData<Int>()
    val isPlaying = MutableLiveData<Boolean>()
    val mutedColor = MutableLiveData<Int>()
    val decodedArt = MutableLiveData<Bitmap>()

    /*val currentSong = MutableLiveData<String>()
    val currentArtist = MutableLiveData<String>()
    val currentUri = MutableLiveData<Uri>()
    val songLength = MutableLiveData<Int>()*/

}