package com.msc24x.player

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CommonViewModel : ViewModel() {

    private var str = "0"
    val currentSong = MutableLiveData<String>()
    val currentArtist = MutableLiveData<String>()
    val currentPosition = MutableLiveData<Int>()
    val currentUri = MutableLiveData<Uri>()
    val busy = MutableLiveData<String>()
    val songLength = MutableLiveData<Int>()


/*
    fun getSong(): LiveData<String> {
        return currentSong
    }

    fun setSong(string: String) {
        currentSong.value = string
    }

    fun getArtist(): LiveData<String> {
        return currentArtist
    }

    fun setArtist(string: String) {
        currentArtist.value = string
    }
*/


    fun progressToString(p: Int): String {

        val min = p / 1000 / 60
        val sec = p / 1000 % 60
        if (min == 0) {
            str += "0:"
        } else if (min < 10) {
            str = min.toString() + ":"
        } else {
            str = min.toString() + ":"
        }
        if (sec < 10) {
            str = str + "0" + sec.toString()
        } else {
            str += sec.toString()
        }

        return str
    }

}