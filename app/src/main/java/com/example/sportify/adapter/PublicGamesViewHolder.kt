package com.example.sportify.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.OnPublicGameClickListener
import com.example.sportify.R
import com.example.sportify.databinding.PublicGameCardBinding
import com.example.sportify.model.AuthManager
import com.example.sportify.model.Game
import com.example.sportify.model.Model
import com.example.sportify.model.WeatherService
import com.squareup.picasso.Picasso

class PublicGamesViewHolder(
    private val binding: PublicGameCardBinding,
    listener: OnPublicGameClickListener?
): RecyclerView.ViewHolder(binding.root) {
    private var game: Game? = null
    private var weatherRequestInProgress = false
    private var position = -1

    init {
        binding.approvalIcon?.apply {
            setOnClickListener {
                (tag as? Int)?.let { tag ->
                    game?.let {
                        val isAlreadyApproved = it.approvals.contains(AuthManager.shared.userId)
                        if (isAlreadyApproved) {
                            it.approvals.remove(AuthManager.shared.userId)
                        } else {
                            it.approvals.add(AuthManager.shared.userId)
                        }
                    }

                    Model.shared.addGame(game!!, null) {
                        listener?.onApprovalClicked(tag)
                    }
                }
            }
        }
    }

    // Update the bind method for more stable weather display
    fun bind(game: Game?, position: Int) {
        if (game?.userId != null) {
            Model.shared.getUserById(userId = game.userId,
                { user ->
                    binding.userDetails.text = "${user?.name}, ${user?.age}"
                },
                {
                    binding.userDetails.text = "Username"
                }
            )
        }


        this.game = game
        this.position = position

        binding.gameDescription.text = game?.description ?: ""
        binding.gameLocation.text = game?.location

        val isApprovedByUser = game?.approvals?.contains(AuthManager.shared.userId) ?: false

        // Set approval counts and button state
        binding.approvalsCount.text = "${game?.approvals?.size ?: 0} / ${game?.numberOfPlayers ?: 0}"
        binding.approvalIcon.apply {
            setImageResource(
                if (isApprovedByUser) R.drawable.ic_thumb_up_fill else R.drawable.ic_thumb_up
            )
            tag = position
            isEnabled = isApprovedByUser || game?.approvals?.size != game?.numberOfPlayers
        }

        // Display game image
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
        binding.gameWeather.text = "Trying to load weather..."
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
}