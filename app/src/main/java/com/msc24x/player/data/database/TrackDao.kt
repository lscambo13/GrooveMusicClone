package com.msc24x.player.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query(value = "SELECT * FROM tracks WHERE title LIKE '%'||:search||'%'")
    fun getAllTracks(search: String): Flow<List<Track>>

    @Insert
    suspend fun insert(track: Track)

    @Update
    suspend fun update(track: Track)

    @Delete
    suspend fun delete(track: Track)
}