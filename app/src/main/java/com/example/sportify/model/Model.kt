package com.example.sportify.model

import android.os.Looper
import androidx.core.os.HandlerCompat
import com.example.sportify.base.EmptyCallback
import com.example.sportify.base.GamesCallback
import com.example.sportify.model.dao.AppLocalDb
import com.example.sportify.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class Model private constructor() {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executer = Executors.newSingleThreadExecutor()
    private var mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    companion object {
        val shared = Model()
    }

    fun getAllGames(callback: GamesCallback) {
        executer.execute {
            val games = database.gamesDao().getAllGames()

            mainHandler.post {
                callback(games)
            }
        }
    }

    fun getGameById(gameId: String, callback: (Game) -> Unit) {
        executer.execute {
            val game = database.gamesDao().getGamesById(gameId)

            mainHandler.post {
                callback(game)
            }
        }
    }

    fun addGame(game: Game, callback: EmptyCallback) {
        executer.execute {
            database.gamesDao().insertAll(game)

            mainHandler.post {
                callback()
            }
        }
    }

    fun deleteGame(game: Game, callback: EmptyCallback) {
        executer.execute {
            database.gamesDao().delete(game)

            mainHandler.post {
                callback()
            }
        }
    }
}