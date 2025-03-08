package com.example.sportify

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.sportify.model.Game
import com.example.sportify.model.Model

class MyGamesListViewModel: ViewModel() {
    var games: LiveData<List<Game>> = Model.shared.getGamesForCurrentUser()

    fun refreshAllGames() {
        Model.shared.refreshAllGames()
    }
}
