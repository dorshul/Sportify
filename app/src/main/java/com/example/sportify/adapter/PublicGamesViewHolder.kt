package com.example.sportify.adapter

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.OnPublicGameClickListener
import com.example.sportify.R
import com.example.sportify.model.Game
import com.example.sportify.model.Model

class PublicGamesViewHolder(
    itemView: View,
    listener: OnPublicGameClickListener?
): RecyclerView.ViewHolder(itemView) {
    private var userDetailsTextView: TextView? = null
    private var descriptionTextView: TextView? = null
    private var locationTextView: TextView? = null
    private var weatherTextView: TextView? = null
    private var approvalTextView: TextView? = null
    private var approvalButton: ImageButton? = null

    private var game: Game? = null

    init {
        userDetailsTextView = itemView.findViewById(R.id.user_details)
        descriptionTextView = itemView.findViewById(R.id.game_description)
        locationTextView = itemView.findViewById(R.id.game_location)
        weatherTextView = itemView.findViewById(R.id.game_weather)
        approvalTextView = itemView.findViewById(R.id.approvals_count)
        approvalButton = itemView.findViewById(R.id.approval_icon)

        approvalButton?.apply {
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

                    Model.shared.addGame(game!!) {
                        listener?.onApprovalClicked(tag)
                    }
                }
            }
        }
    }

    fun bind(game: Game?, position: Int) {
        this.game = game
        userDetailsTextView?.text = game?.userId
        descriptionTextView?.text = game?.description ?: ""
        locationTextView?.text = game?.location
        weatherTextView?.text = "25°C ☀️"
        approvalTextView?.text = "${game?.approvals ?: 0} / ${game?.numberOfPlayers ?: 0}"
        approvalButton?.apply {
            setImageResource(
                if (game?.isApproved == true) R.drawable.ic_thumb_up_fill else R.drawable.ic_thumb_up
            )
            tag = position
            isEnabled = game?.isApproved == true || game?.approvals != game?.numberOfPlayers
        }
    }
}