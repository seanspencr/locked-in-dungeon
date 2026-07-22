package com.example.lockedindungeon.di

import android.content.Context
import androidx.room.Room
import com.example.lockedindungeon.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDatabaseModule {

    private val LOCK = Any()

    @Provides
    @Singleton
    fun initRoomDataBase(@ApplicationContext context: Context): AppDatabase {
        return synchronized(LOCK) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "locked_in_dungeon"
            )
            .fallbackToDestructiveMigration(true)
            .build()
        }
    }
}