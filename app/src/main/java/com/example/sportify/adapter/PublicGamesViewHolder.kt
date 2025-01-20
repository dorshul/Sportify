package com.example.sportify.adapter

import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.OnPublicGameClickListener
import com.example.sportify.R
import com.example.sportify.databinding.PublicGameCardBinding
import com.example.sportify.model.Game
import com.example.sportify.model.Model
import com.squareup.picasso.Picasso

class PublicGamesViewHolder(
    private val binding: PublicGameCardBinding,
    listener: OnPublicGameClickListener?
): RecyclerView.ViewHolder(binding.root) {
    private var game: Game? = null

    init {
        binding.approvalIcon?.apply {
            setOnClickListener {
                (tag as? Int)?.let { tag ->
                    game?.let {
                        it.isApproved = !it.isApproved
                        if (it.isApproved) {
                            it.approvals = (it.approvals ?: 0) + 1
                        } else if (it.approvals > 0) {
                            it.approvals = (game?.approvals ?: 0) - 1
                        }
                    }

                    Model.shared.addGame(game!!, null) {
                        listener?.onApprovalClicked(tag)
                    }
                }
            }
        }
    }

    fun bind(game: Game?, position: Int) {
        this.game = game
        binding.userDetails.text = game?.userId
        binding.gameDescription.text = game?.description ?: ""
        binding.gameLocation.text = game?.location
        binding.gameWeather.text = "25°C ☀️"
        binding.approvalsCount.text = "${game?.approvals ?: 0} / ${game?.numberOfPlayers ?: 0}"
        binding.approvalIcon.apply {
            setImageResource(
                if (game?.isApproved == true) R.drawable.ic_thumb_up_fill else R.drawable.ic_thumb_up
            )
            tag = position
            isEnabled = game?.isApproved == true || game?.approvals != game?.numberOfPlayers
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