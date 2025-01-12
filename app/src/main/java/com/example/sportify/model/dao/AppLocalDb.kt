package com.example.sportify.model.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sportify.base.MyApplication
import com.example.sportify.model.Game

@Database(entities = [Game::class], version = 5)
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract fun gamesDao(): GameDao
}

object AppLocalDb {

    val database: AppLocalDbRepository by lazy {

        val context = MyApplication.Globals.context ?: throw IllegalStateException("Application context is missing")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "dbFileName.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}