package com.msc24x.player

import android.graphics.Bitmap
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.msc24x.player.data.database.Track
import com.msc24x.player.data.database.TrackDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest


class CommonViewModel @ViewModelInject constructor(private val trackDao: TrackDao) : ViewModel() {

    //var trackPlaylist = mutableListOf<Track>()
    val currentTrack = MutableLiveData<Track>()

    val searchQuery = MutableStateFlow("")
    val tracksFlow = searchQuery.flatMapLatest {
        trackDao.getAllTracks(it)
    }
    val tracks = tracksFlow.asLiveData()


    val currentPosition = MutableLiveData<Int>()
    val isPlaying = MutableLiveData<Boolean>()
    val mutedColor = MutableLiveData<Int>()
    val decodedArt = MutableLiveData<Bitmap>()

    /*val currentSong = MutableLiveData<String>()
    val currentArtist = MutableLiveData<String>()
    val currentUri = MutableLiveData<Uri>()
    val songLength = MutableLiveData<Int>()*/

}