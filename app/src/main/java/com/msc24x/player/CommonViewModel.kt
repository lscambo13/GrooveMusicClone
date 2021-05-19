package com.msc24x.player

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class CommonViewModel : ViewModel() {

    val currentSong = MutableLiveData<String>()
    val currentArtist = MutableLiveData<String>()
    val currentPosition = MutableLiveData<Int>()
    val currentUri = MutableLiveData<Uri>()
    val busy = MutableLiveData<Boolean>()
    val songLength = MutableLiveData<Int>()
    val mutedColor = MutableLiveData<Int>()
    val decodedArt = MutableLiveData<Bitmap>()


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
        var str = ""
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