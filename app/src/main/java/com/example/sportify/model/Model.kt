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
import java.util.concurrent.ConcurrentHashMap

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

    // Use ConcurrentHashMap for thread safety
    private val gamesBeingFetched = ConcurrentHashMap<String, Long>()
    private val weatherLastFetched = ConcurrentHashMap<String, Long>()
    private val WEATHER_REFETCH_INTERVAL = 30 * 60 * 1000 // 30 minutes

    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()

    companion object {
        val shared = Model()
        private const val TAG = "Model"
    }

    init {
        listenForGameChanges() // Start real-time Firestore sync
    }



    fun fetchWeatherForGame(game: Game, callback: (Boolean) -> Unit) {
        if (game.location.isEmpty()) {
            Log.d(TAG, "Cannot fetch weather for empty location")
            callback(false)
            return
        }

        // Check if we've recently fetched weather for this game
        val currentTime = System.currentTimeMillis()
        val lastFetched = weatherLastFetched[game.id]
        if (lastFetched != null && (currentTime - lastFetched < WEATHER_REFETCH_INTERVAL)) {
            Log.d(TAG, "Recently fetched weather for game ${game.id}, using existing data")
            callback(true)
            return
        }

        // Check if we're already fetching weather for this game
        if (gamesBeingFetched.containsKey(game.id)) {
            Log.d(TAG, "Already fetching weather for game: ${game.id}")
            callback(false)
            return
        }

        // Check if game already has weather data that's recent
        if (!game.weatherTemp.isNullOrEmpty() && WeatherService.hasValidCache(game.location)) {
            Log.d(TAG, "Game already has weather data: ${game.weatherTemp}")
            callback(true)
            return
        }

        // Mark this game as being fetched
        gamesBeingFetched[game.id] = currentTime

        Log.d(TAG, "Fetching weather for location: ${game.location}")
        weatherService.getWeatherByCity(game.location) { weatherInfo, error ->
            // Remove from fetching set when done
            gamesBeingFetched.remove(game.id)

            if (weatherInfo != null) {
                Log.d(TAG, "Weather fetched successfully: ${weatherInfo.formatForDisplay()}")

                // Mark as recently fetched
                weatherLastFetched[game.id] = System.currentTimeMillis()

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

                // Save the updated game to both Firebase and local DB
                executor.execute {
                    // Update local database first for immediate UI update
                    database.gamesDao().insertAll(updatedGame)

                    // Then update Firebase
                    mainHandler.post {
                        firebaseModel.addGame(updatedGame) {
                            Log.d(TAG, "Game updated with weather data")
                            callback(true)
                        }
                    }
                }
            } else {
                Log.e(TAG, "Error fetching weather: $error")
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
        // First check local database for immediate response
        executor.execute {
            try {
                val localGame = database.gamesDao().getGamesById(gameId)
                mainHandler.post {
                    callback(localGame)

                    // Also fetch from Firebase to ensure latest data
                    firebaseModel.getGameById(gameId) { firebaseGame ->
                        if (firebaseGame != null && (localGame == null ||
                                    firebaseGame.lastUpdated != localGame.lastUpdated)) {
                            // Update local cache if Firebase has newer data
                            executor.execute {
                                database.gamesDao().insertAll(firebaseGame)
                                mainHandler.post {
                                    callback(firebaseGame)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching game from local DB: ${e.message}")
                mainHandler.post {
                    // Fall back to Firebase
                    firebaseModel.getGameById(gameId, callback)
                }
            }
        }
    }

    fun addGame(game: Game, image: Bitmap?, callback: EmptyCallback) {
        // Set the user ID to the current authenticated user
        val gameWithUserId = game.copy(userId = AuthManager.shared.userId)

        // First update local database for immediate UI feedback
        executor.execute {
            database.gamesDao().insertAll(gameWithUserId)

            mainHandler.post {
                // Then update Firebase
                firebaseModel.addGame(gameWithUserId) {
                    image?.let {
                        cloudinaryModel.uploadImage(
                            bitmap = image,
                            gameId = game.id,
                            onSuccess = { uri ->
                                if (!uri.isNullOrBlank()) {
                                    val gm = gameWithUserId.copy(pictureUrl = uri)
                                    // Update local DB
                                    executor.execute {
                                        database.gamesDao().insertAll(gm)
                                        mainHandler.post {
                                            firebaseModel.addGame(gm, callback)
                                        }
                                    }
                                } else {
                                    callback()
                                }
                            },
                            onError = { callback() }
                        )
                    } ?: callback()
                }
            }
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

    // Clear all caches (used for testing or when user logs out)
    fun clearCaches() {
        gamesBeingFetched.clear()
        weatherLastFetched.clear()
    }

    fun clearWeatherData(game: Game, callback: (Boolean) -> Unit) {
        Log.d(TAG, "Explicitly clearing weather data for game ${game.id}")

        // Remove from weather tracking
        weatherLastFetched.remove(game.id)
        gamesBeingFetched.remove(game.id)

        // Create updated game with null weather fields
        val updatedGame = Game(
            id = game.id,
            userId = game.userId,
            pictureUrl = game.pictureUrl,
            location = game.location,
            description = game.description,
            numberOfPlayers = game.numberOfPlayers,
            approvals = game.approvals,
            isApproved = game.isApproved,
            lastUpdated = System.currentTimeMillis(), // Update timestamp to force refresh
            weatherTemp = null,
            weatherDescription = null,
            weatherIcon = null
        )

        // First update local database for immediate UI feedback
        executor.execute {
            try {
                database.gamesDao().insertAll(updatedGame)

                // Then update Firebase with explicit field removal
                mainHandler.post {
                    // Use the deleteWeatherFields method to ensure fields are removed
                    firebaseModel.deleteWeatherFields(game.id) {
                        Log.d(TAG, "Weather data cleared for game: ${game.id}")
                        callback(true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing weather data: ${e.message}")
                mainHandler.post {
                    callback(false)
                }
            }
        }
    }

    fun fetchWeatherForGame(game: Game, forceRefresh: Boolean = false, callback: (Boolean) -> Unit) {
        if (game.location.isEmpty()) {
            Log.d(TAG, "Cannot fetch weather for empty location")
            callback(false)
            return
        }

        // If force refresh is true, don't use cache checks
        if (!forceRefresh) {
            // Check if we've recently fetched weather for this game
            val currentTime = System.currentTimeMillis()
            val lastFetched = weatherLastFetched[game.id]
            if (lastFetched != null && (currentTime - lastFetched < WEATHER_REFETCH_INTERVAL)) {
                Log.d(TAG, "Recently fetched weather for game ${game.id}, using existing data")
                callback(true)
                return
            }

            // Check if game already has weather data that's recent
            if (!game.weatherTemp.isNullOrEmpty() && WeatherService.hasValidCache(game.location)) {
                Log.d(TAG, "Game already has weather data: ${game.weatherTemp}")
                callback(true)
                return
            }
        } else {
            Log.d(TAG, "Forcing weather refresh for game: ${game.id}")
            // Remove this location from cache to force a fresh API call
            WeatherService.removeFromCache(game.location)
        }

        // Check if we're already fetching weather for this game
        if (gamesBeingFetched.containsKey(game.id)) {
            Log.d(TAG, "Already fetching weather for game: ${game.id}")
            callback(false)
            return
        }

        // Mark this game as being fetched
        gamesBeingFetched[game.id] = System.currentTimeMillis()

        Log.d(TAG, "Fetching weather for location: ${game.location}")
        weatherService.getWeatherByCity(game.location) { weatherInfo, error ->
            // Remove from fetching set when done
            gamesBeingFetched.remove(game.id)

            if (weatherInfo != null) {
                Log.d(TAG, "Weather fetched successfully: ${weatherInfo.formatForDisplay()}")

                // Mark as recently fetched
                weatherLastFetched[game.id] = System.currentTimeMillis()

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

                // Save the updated game to both Firebase and local DB
                executor.execute {
                    // Update local database first for immediate UI update
                    database.gamesDao().insertAll(updatedGame)

                    // Then update Firebase
                    mainHandler.post {
                        firebaseModel.addGame(updatedGame) {
                            Log.d(TAG, "Game updated with weather data")
                            callback(true)
                        }
                    }
                }
            } else {
                Log.e(TAG, "Error fetching weather: $error")

                // If this was a forced refresh (due to location change), clear the weather data
                if (forceRefresh) {
                    Log.d(TAG, "Clearing weather data due to failed fetch after location change")

                    // Create a game object with weather data nullified
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
                        weatherTemp = null,
                        weatherDescription = null,
                        weatherIcon = null
                    )

                    // Save the updated game with null weather data
                    executor.execute {
                        database.gamesDao().insertAll(updatedGame)

                        mainHandler.post {
                            firebaseModel.addGame(updatedGame) {
                                callback(false)
                            }
                        }
                    }
                } else {
                    callback(false)
                }
            }
        }
    }}