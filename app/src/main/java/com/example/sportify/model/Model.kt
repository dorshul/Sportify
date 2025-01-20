package com.example.sportify.model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
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
    enum class Storage {
        FIREBASE,
        CLOUDINARY
    }

    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()

    companion object {
        val shared = Model()
    }

    fun getAllGames(callback: GamesCallback) {
//        executer.execute {
//            val games = database.gamesDao().getAllGames()
//
//            mainHandler.post {
//                callback(games)
//            }
//        }
        firebaseModel.getAllGames(callback)
    }

    fun getGameById(gameId: String, callback: (Game?) -> Unit) {
//        executer.execute {
//            val game = database.gamesDao().getGamesById(gameId)
//
//            mainHandler.post {
//                callback(game)
//            }
//        }
        firebaseModel.getGameById(gameId, callback)
    }

    fun addGame(game: Game, image: Bitmap?, callback: EmptyCallback) {
        Log.d("DEBUG", "jeyyyyyyyyyyyyyyyyyyyyyyyyy")
//        executer.execute {
//            database.gamesDao().insertAll(game)
//
//            mainHandler.post {
//                callback()
//            }
//        }
//        firebaseModel.addGame(game, callback)
        firebaseModel.addGame(game) {
            image?.let {
                Log.d("DEBUG", "sdgfsdgfg")
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
//        executer.execute {
//            database.gamesDao().delete(game)
//
//            mainHandler.post {
//                callback()
//            }
//        }
        firebaseModel.deleteGame(game, callback)
    }
}