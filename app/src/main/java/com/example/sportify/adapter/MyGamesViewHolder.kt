package com.example.sportify.adapter

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.OnMyGameClickListener
import com.example.sportify.R
import com.example.sportify.databinding.MyGameCardBinding
import com.example.sportify.model.Game
import com.example.sportify.model.Model
import com.example.sportify.model.WeatherService
import com.squareup.picasso.Picasso

class MyGamesViewHolder(
    private val binding: MyGameCardBinding,
    listener: OnMyGameClickListener?
): RecyclerView.ViewHolder(binding.root) {
    private var game: Game? = null
    private var weatherRequestInProgress = false
    private var position = -1

    // Update the bind method to handle weather display
    fun bind(game: Game?, position: Int) {
        this.game = game
        this.position = position

        binding.gameDescription.text = game?.description ?: ""
        binding.gameLocation.text = game?.location

        // Display approvals count
        binding.approvalsCount.text = "${game?.approvals?.size ?: 0} / ${game?.numberOfPlayers ?: 0}"

        // Set up delete and edit buttons
        binding.deleteGameButton.apply {
            tag = position
        }
        binding.editGameButton.apply {
            tag = position
        }

        // Display the game image
        game?.pictureUrl?.let {
            if (it.isNotBlank()) {
                Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_broken_image)
                    .into(binding.gamePicture)
            }
        }

        // Handle weather display
        updateWeatherDisplay()
    }

    private fun updateWeatherDisplay() {
        // Skip if game is null
        val currentGame = game ?: return

        // If we already have weather data, display it
        if (!currentGame.weatherTemp.isNullOrEmpty()) {
            val weatherEmoji = getWeatherEmoji(currentGame.weatherIcon)
            binding.gameWeather.text = "${currentGame.weatherTemp} $weatherEmoji"
            binding.gameWeather.visibility = View.VISIBLE
            return
        }

        // If location is empty, hide weather display
        if (currentGame.location.isNullOrEmpty()) {
            binding.gameWeather.visibility = View.GONE
            return
        }

        // If we're already fetching weather, show loading
        if (weatherRequestInProgress) {
            binding.gameWeather.text = "Loading weather..."
            binding.gameWeather.visibility = View.VISIBLE
            return
        }

        // Check if we have cached weather data for this location
        if (WeatherService.hasValidCache(currentGame.location)) {
            val cachedWeather = WeatherService.getCachedWeather(currentGame.location)
            if (cachedWeather != null) {
                binding.gameWeather.text = cachedWeather.formatForDisplay()
                binding.gameWeather.visibility = View.VISIBLE

                // We should still update the game object in the database with this weather data
                // But we don't need to wait for it or show loading
                updateGameWithCachedWeather(currentGame, cachedWeather)
                return
            }
        }

        // If we got here, we need to fetch weather - show loading state
        binding.gameWeather.text = "Loading weather..."
        binding.gameWeather.visibility = View.VISIBLE
        weatherRequestInProgress = true

        // Fetch weather data
        Model.shared.fetchWeatherForGame(currentGame) { success ->
            weatherRequestInProgress = false

            // Only update UI if view is still attached and position hasn't changed
            if (binding.root.isAttachedToWindow && this.position == position) {
                if (success) {
                    // Get latest game data with weather
                    Model.shared.getGameById(currentGame.id) { updatedGame ->
                        if (binding.root.isAttachedToWindow && this.position == position) {
                            updatedGame?.let {
                                game = it // Update our local reference
                                val weatherEmoji = getWeatherEmoji(it.weatherIcon)
                                binding.gameWeather.text = "${it.weatherTemp} $weatherEmoji"
                            }
                        }
                    }
                } else {
                    binding.gameWeather.text = "Weather unavailable"
                }
            }
        }
    }

    private fun updateGameWithCachedWeather(game: Game, weather: WeatherService.WeatherInfo) {
        val updatedGame = game.copy(
            weatherTemp = weather.formattedTemperature(),
            weatherDescription = weather.description,
            weatherIcon = weather.icon
        )

        // Update in database without UI callbacks
        Model.shared.addGame(updatedGame, null) { }
    }

    // Helper method to get weather emoji
    private fun getWeatherEmoji(icon: String?): String {
        if (icon.isNullOrEmpty()) return "☀️"

        return when {
            icon.contains("01") -> "☀️" // clear sky
            icon.contains("02") -> "⛅" // few clouds
            icon.contains("03") || icon.contains("04") -> "☁️" // clouds
            icon.contains("09") || icon.contains("10") -> "🌧️" // rain
            icon.contains("11") -> "⛈️" // thunderstorm
            icon.contains("13") -> "❄️" // snow
            icon.contains("50") -> "🌫️" // mist
            else -> "🌤️" // default
        }
    }

    init {
        binding.deleteGameButton?.apply {
            setOnClickListener {
                (tag as? Int)?.let { tag ->
                    game?.let { gameToDelete ->
                        Model.shared.deleteGame(gameToDelete) {
                            Model.shared.refreshAllGames()
                        }
                    }
                }
            }
        }

        binding.editGameButton?.apply {
            setOnClickListener {
                listener?.onEditClick(game)
            }
        }
    }
}