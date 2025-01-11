package com.example.sportify.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.OnPublicGameClickListener
import com.example.sportify.R
import com.example.sportify.model.Game

class PublicGamesRecyclerAdapter(private var games: List<Game>?): RecyclerView.Adapter<PublicGamesViewHolder>() {

    var listener: OnPublicGameClickListener? = null

    fun update(games: List<Game>?) {
        this.games = games
    }

    override fun getItemCount(): Int = games?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicGamesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.public_game_card,
            parent,
            false
        )
        return PublicGamesViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(holder: PublicGamesViewHolder, position: Int) {
        holder.bind(
            game = games?.get(position),
            position = position
        )
    }

    fun onApprovalClicked(position: Int) {
        notifyItemChanged(position)
    }
}