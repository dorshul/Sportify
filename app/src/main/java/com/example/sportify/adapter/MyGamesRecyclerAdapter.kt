package com.example.sportify.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.MyGamesListFragment
import com.example.sportify.OnMyGameClickListener
import com.example.sportify.OnPublicGameClickListener
import com.example.sportify.R
import com.example.sportify.model.Game

class MyGamesRecyclerAdapter(private var games: List<Game>?): RecyclerView.Adapter<MyGamesViewHolder>() {

    var listener: OnMyGameClickListener? = null

    fun update(games: List<Game>?) {
        this.games = games
    }

    override fun getItemCount(): Int = games?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyGamesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.my_game_card,
            parent,
            false
        )
        return MyGamesViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(holder: MyGamesViewHolder, position: Int) {
        holder.bind(
            game = games?.get(position),
            position = position
        )
    }
}