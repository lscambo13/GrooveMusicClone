package com.msc24x.player.data.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "tracks")
@Parcelize
data class Track(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "uri") val uri: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "artist_name") val artist_name: String,
    @ColumnInfo(name = "album_name") val album_name: String,
    @ColumnInfo(name = "duration") val duration: Long
) : Parcelable
