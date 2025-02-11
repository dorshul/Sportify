package com.example.sportify

import androidx.lifecycle.ViewModel
import com.example.sportify.model.Game

class MyGamesListViewModel: ViewModel() {
    var games: List<Game>? = null
}