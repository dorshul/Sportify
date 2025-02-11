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
import com.squareup.picasso.Picasso

class MyGamesViewHolder(
    private val binding: MyGameCardBinding,
    listener: OnMyGameClickListener?
): RecyclerView.ViewHolder(binding.root) {
    private var game: Game? = null

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

    fun bind(game: Game?, position: Int) {
        this.game = game
        binding.gameDescription.text = game?.description ?: ""
        binding.gameLocation.text = game?.location
        binding.gameWeather.text = "25°C ☀️"
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
}