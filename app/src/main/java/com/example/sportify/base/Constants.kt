package com.example.sportify.base

import com.example.sportify.model.Game

typealias GamesCallback = (List<Game>) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {
    object Collections {
        const val GAMES = "games"
        const val USERS = "users"
    }
}