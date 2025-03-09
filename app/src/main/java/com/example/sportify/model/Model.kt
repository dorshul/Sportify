package com.example.sportify.model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sportify.base.EmptyCallback
import com.example.sportify.base.GamesCallback
import com.example.sportify.model.dao.AppLocalDb
import com.example.sportify.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class Model private constructor() {

    enum class LoadingState {
        LOADING,
        LOADED
    }

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()
    private var mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    val games: LiveData<List<Game>> = database.gamesDao().getAllGames()
    val loadingState: MutableLiveData<LoadingState> = MutableLiveData<LoadingState>()
    private val weatherService = WeatherService()

    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()

    companion object {
        val shared = Model()
    }
    init {
        listenForGameChanges() // Start real-time Firestore sync
    }


    fun fetchWeatherForGame(game: Game, callback: (Boolean) -> Unit) {
        if (game.location.isEmpty()) {
            Log.d("Model", "Cannot fetch weather for empty location")
            callback(false)
            return
        }

        Log.d("Model", "Fetching weather for location: ${game.location}")
        weatherService.getWeatherByCity(game.location) { weatherInfo, error ->
            if (weatherInfo != null) {
                Log.d("Model", "Weather fetched successfully: ${weatherInfo.formatForDisplay()}")

                // Create a new game object with the weather data
                val updatedGame = Game(
                    id = game.id,
                    userId = game.userId,
                    pictureUrl = game.pictureUrl,
                    location = game.location,
                    description = game.description,
                    numberOfPlayers = game.numberOfPlayers,
                    approvals = game.approvals,
                    isApproved = game.isApproved,
                    lastUpdated = game.lastUpdated,
                    weatherTemp = weatherInfo.formattedTemperature(),
                    weatherDescription = weatherInfo.description,
                    weatherIcon = weatherInfo.icon
                )

                // Save the updated game
                firebaseModel.addGame(updatedGame) {
                    Log.d("Model", "Game updated with weather data")
                    callback(true)
                }
            } else {
                Log.e("Model", "Error fetching weather: $error")
                callback(false)
            }
        }
    }

    fun refreshAllGames() {
        loadingState.postValue(LoadingState.LOADING)
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
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }


    fun getGameById(gameId: String, callback: (Game?) -> Unit) {
        firebaseModel.getGameById(gameId, callback)
    }

    fun addGame(game: Game, image: Bitmap?, callback: EmptyCallback) {
        // Set the user ID to the current authenticated user
        val gameWithUserId = game.copy(userId = AuthManager.shared.userId)

        firebaseModel.addGame(gameWithUserId) {
            image?.let {
                cloudinaryModel.uploadImage(
                    bitmap = image,
                    gameId = game.id,
                    onSuccess = { uri ->
                        if (!uri.isNullOrBlank()) {
                            val gm = gameWithUserId.copy(pictureUrl = uri)
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
        executor.execute {
            database.gamesDao().delete(game)

            mainHandler.post {
                firebaseModel.deleteGame(game, callback)
            }
        }
    }

    fun getGamesForCurrentUser(): LiveData<List<Game>> {
        val userId = AuthManager.shared.userId
        return if (userId.isNotEmpty()) {
            database.gamesDao().getGamesByUserIdLiveData(userId)
        } else {
            games
        }
    }


    private fun listenForGameChanges() {
        firebaseModel.listenForGameChanges{ updatedGames, deletedGames ->
            executor.execute {
                // Insert or update Games in Room
                database.gamesDao().insertAll(*updatedGames.toTypedArray())

                // Delete Games from Room if they were removed in Firebase
                deletedGames.forEach { game ->
                    database.gamesDao().delete(game)
                }
            }
        }
    }
}