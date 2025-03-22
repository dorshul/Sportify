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
        val currentGame = game ?: return

        // If we already have weather data, display it
        if (!currentGame.weatherTemp.isNullOrEmpty()) {
            val weatherEmoji = getWeatherEmoji(currentGame.weatherIcon)
            binding.gameWeather.text = "${currentGame.weatherTemp} $weatherEmoji"
            binding.gameWeather.visibility = View.VISIBLE
            return
        }

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

                updateGameWithCachedWeather(currentGame, cachedWeather)
                return
            }
        }

        binding.gameWeather.text = "Trying to load weather..."
        binding.gameWeather.visibility = View.VISIBLE
        weatherRequestInProgress = true

        Model.shared.fetchWeatherForGame(currentGame) { success ->
            weatherRequestInProgress = false

            if (binding.root.isAttachedToWindow && this.position == position) {
                if (success) {
                    Model.shared.getGameById(currentGame.id) { updatedGame ->
                        if (binding.root.isAttachedToWindow && this.position == position) {
                            updatedGame?.let {
                                game = it
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

        Model.shared.addGame(updatedGame, null) { }
    }

    private fun getWeatherEmoji(icon: String?): String {
        if (icon.isNullOrEmpty()) return "☀️"

        return when {
            icon.contains("01") -> "☀️"
            icon.contains("02") -> "⛅"
            icon.contains("03") || icon.contains("04") -> "☁️"
            icon.contains("09") || icon.contains("10") -> "🌧️"
            icon.contains("11") -> "⛈️"
            icon.contains("13") -> "❄️"
            icon.contains("50") -> "🌫️"
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