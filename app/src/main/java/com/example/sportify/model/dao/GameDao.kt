package com.example.sportify.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportify.model.Game

@Dao
interface GameDao {

    @Query("SELECT * FROM Game ORDER BY userId")
    fun getAllGames(): List<Game>

    @Query("SELECT * FROM Game WHERE userId =:userId")
    fun getGamesByUserId(userId: String): Game

    @Query("SELECT * FROM Game WHERE userId !=:userId")
    fun getGamesOfOtherUsers(userId: String): Game

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg game: Game)

    @Delete
    fun delete(game: Game)
}