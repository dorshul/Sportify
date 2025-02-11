package com.example.sportify

import androidx.lifecycle.ViewModel
import com.example.sportify.model.Game

class PublicGamesListViewModel: ViewModel() {
    var games: List<Game>? = null
}