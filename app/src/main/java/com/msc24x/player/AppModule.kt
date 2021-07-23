package com.msc24x.player

import android.app.Application
import androidx.room.Room
import com.msc24x.player.data.database.TrackDao
import com.msc24x.player.data.database.TrackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    fun provideTrackDao(trackDatabase: TrackDatabase)
            : TrackDao = trackDatabase.getTrackDao()

    @Provides
    @Singleton
    fun provideTrackDatabase(app: Application)
            : TrackDatabase = Room.databaseBuilder(app, TrackDatabase::class.java, "trackDatabase")
        .build()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope((SupervisorJob()))
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope