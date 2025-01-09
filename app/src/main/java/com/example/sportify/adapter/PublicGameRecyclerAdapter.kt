package com.example.sportify.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.OnGameClickListener
import com.example.sportify.R
import com.example.sportify.model.Game

class PublicGameRecyclerAdapter(private var games: List<Game>?): RecyclerView.Adapter<PublicGameViewHolder>() {

    var listener: OnGameClickListener? = null

    fun update(games: List<Game>?) {
        this.games = games
    }

    override fun getItemCount(): Int = games?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicGameViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.public_game_card,
            parent,
            false
        )
        return PublicGameViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(holder: PublicGameViewHolder, position: Int) {
        holder.bind(
            game = games?.get(position),
            position = position
        )
    }

    fun onApprovalClicked(position: Int) {
        notifyItemChanged(position)
    }
}