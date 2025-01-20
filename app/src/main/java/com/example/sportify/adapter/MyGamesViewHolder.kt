package com.example.sportify.adapter

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.OnMyGameClickListener
import com.example.sportify.R
import com.example.sportify.model.Game
import com.example.sportify.model.Model

class MyGamesViewHolder(
    itemView: View,
    listener: OnMyGameClickListener?
): RecyclerView.ViewHolder(itemView) {
    private var descriptionTextView: TextView? = null
    private var locationTextView: TextView? = null
    private var weatherTextView: TextView? = null
    private var approvalTextView: TextView? = null
    private var deleteImageButton: ImageButton? = null
    private var editImageButton: ImageButton? = null

    private var game: Game? = null

    init {
        descriptionTextView = itemView.findViewById(R.id.game_description)
        locationTextView = itemView.findViewById(R.id.game_location)
        weatherTextView = itemView.findViewById(R.id.game_weather)
        approvalTextView = itemView.findViewById(R.id.approvals_count)
        deleteImageButton = itemView.findViewById((R.id.delete_game_button))
        editImageButton = itemView.findViewById((R.id.edit_game_button))
        deleteImageButton?.apply {
            setOnClickListener {
                (tag as? Int)?.let { tag ->
                    Model.shared.deleteGame(game!!) {
                        Navigation.findNavController(itemView).popBackStack()
                    }
                }
            }
        }

        editImageButton?.apply {
            setOnClickListener {
                listener?.onEditClick(game) // Notify parent about the edit click
            }
        }
    }

    fun bind(game: Game?, position: Int) {
        this.game = game
        descriptionTextView?.text = game?.description ?: ""
        locationTextView?.text = game?.location
        weatherTextView?.text = "25°C ☀️"
        approvalTextView?.text = "${game?.approvals ?: 0} / ${game?.numberOfPlayers ?: 0}"
        deleteImageButton?.apply {
            tag = position
        }
        editImageButton?.apply {
            tag = position
        }
    }
}