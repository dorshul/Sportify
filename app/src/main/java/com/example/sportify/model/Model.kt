package com.example.sportify.model

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import com.example.sportify.base.EmptyCallback
import com.example.sportify.base.GamesCallback
import com.example.sportify.model.dao.AppLocalDb
import com.example.sportify.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class Model private constructor() {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()
    private var mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()

    companion object {
        val shared = Model()
    }

    fun getAllGames(callback: GamesCallback) {
        val lastUpdated: Long = Game.lastUpdated
        firebaseModel.getAllGames(lastUpdated) { games ->
            executor.execute {
                var currentTime = lastUpdated
                for (game in games) {
                    database.gamesDao().insertAll(game)
                    game.lastUpdated?.let {
                        if (currentTime < it) {
                            currentTime = it
                        }
                    }
                }
                Game.lastUpdated = currentTime
                val savedStudents = database.gamesDao().getAllGames()
                mainHandler.post {
                    callback(savedStudents)
                }
            }
        }
    }

    fun getGameById(gameId: String, callback: (Game?) -> Unit) {
        firebaseModel.getGameById(gameId, callback)
    }

    fun addGame(game: Game, image: Bitmap?, callback: EmptyCallback) {
        firebaseModel.addGame(game) {
            image?.let {
                cloudinaryModel.uploadImage(
                    bitmap = image,
                    gameId = game.id,
                    onSuccess = { uri ->
                        if (!uri.isNullOrBlank()) {
                            val gm = game.copy(pictureUrl = uri)
                            firebaseModel.addGame(gm, callback)
                        } else {
                            callback()
                        }
                    },
                    onError = { callback() }
                )
            } ?: callback()
        }
    }

    fun deleteGame(game: Game, callback: (Boolean) -> Unit) {
        firebaseModel.deleteGame(game, callback)
    }
}