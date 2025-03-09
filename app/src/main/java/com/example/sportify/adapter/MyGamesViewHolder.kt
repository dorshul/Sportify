package com.example.sportify.adapter

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.Navigation
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
    private var isLoadingWeather = false

    // Update the bind method to handle weather display
    fun bind(game: Game?, position: Int) {
        this.game = game
        binding.gameDescription.text = game?.description ?: ""
        binding.gameLocation.text = game?.location

        // Display weather information if available
        if (!game?.weatherTemp.isNullOrEmpty()) {
            val weatherEmoji = getWeatherEmoji(game?.weatherIcon)
            binding.gameWeather.text = "${game?.weatherTemp} $weatherEmoji"
        } else {
            binding.gameWeather.text = "Loading weather..."

            // Fetch weather if location exists and we aren't already loading
            if (game != null && !game.location.isNullOrEmpty() && !isLoadingWeather) {
                isLoadingWeather = true
                Model.shared.fetchWeatherForGame(game) { success ->
                    isLoadingWeather = false
                    // Weather is updated in the database, it will be loaded next time
                }
            } else {
                binding.gameWeather.text = "25°C ☀️" // Default fallback weather
            }
        }

        binding.approvalsCount.text = "${game?.approvals ?: 0} / ${game?.numberOfPlayers ?: 0}"
        binding.deleteGameButton.apply {
            tag = position
        }
        binding.editGameButton.apply {
            tag = position
        }
        game?.pictureUrl?.let {
            if (it.isNotBlank()) {
                Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_broken_image)
                    .into(binding.gamePicture)
            }
        }
    }

    // Add this helper method to get weather emoji
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
                    Model.shared.deleteGame(game!!) {
                        Model.shared.refreshAllGames()
                    }
                }
            }
        }

        binding.editGameButton?.apply {
            setOnClickListener {
                listener?.onEditClick(game) // Notify parent about the edit click
            }
        }
    }
}