package com.example.sportify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.sportify.model.AuthManager
import com.example.sportify.model.Game
import com.example.sportify.model.Model

class PublicGamesListViewModel: ViewModel() {
    private val userId = AuthManager.shared.userId

    var games: LiveData<List<Game>> = Model.shared.games.map { gamesList ->
        gamesList.filter { game -> game.userId != userId }
    }

    fun refreshAllGames() {
        Model.shared.refreshAllGames()
    }
}