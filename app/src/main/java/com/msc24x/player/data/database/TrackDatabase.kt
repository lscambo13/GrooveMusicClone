package com.msc24x.player.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Track::class], exportSchema = false, version = 1)
abstract class TrackDatabase : RoomDatabase() {
    abstract fun getTrackDao(): TrackDao
}