package com.example.sportify.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportify.model.Game

@Dao
interface GameDao {
    @Query("SELECT * FROM Game ORDER BY userId")
    fun getAllGames(): LiveData<List<Game>>

    @Query("SELECT * FROM Game WHERE userId = :userId")
    fun getGamesByUserId(userId: String): List<Game>

    @Query("SELECT * FROM Game WHERE userId = :userId")
    fun getGamesByUserIdLiveData(userId: String): LiveData<List<Game>>

    @Query("SELECT * FROM Game WHERE id = :gameId")
    fun getGamesById(gameId: String): Game

    @Query("SELECT * FROM Game WHERE userId != :userId")
    fun getGamesOfOtherUsers(userId: String): List<Game>

    @Query("SELECT * FROM Game WHERE userId != :userId")
    fun getGamesOfOtherUsersLiveData(userId: String): LiveData<List<Game>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg game: Game)

    @Delete
    fun delete(game: Game)
}