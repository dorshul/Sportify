package com.example.sportify.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.OnPublicGameClickListener
import com.example.sportify.R
import com.example.sportify.databinding.MyGameCardBinding
import com.example.sportify.databinding.PublicGameCardBinding
import com.example.sportify.model.Game

class PublicGamesRecyclerAdapter(private var games: List<Game>?): RecyclerView.Adapter<PublicGamesViewHolder>() {

    var listener: OnPublicGameClickListener? = null

    fun update(games: List<Game>?) {
        this.games = games
    }

    override fun getItemCount(): Int = games?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicGamesViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding = PublicGameCardBinding.inflate(inflator, parent, false)
        return PublicGamesViewHolder(binding, listener)
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