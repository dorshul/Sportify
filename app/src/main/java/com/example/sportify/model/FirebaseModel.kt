package com.example.sportify.model

import android.util.Log
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase
import com.example.sportify.base.Constants
import com.example.sportify.base.EmptyCallback
import com.example.sportify.base.GamesCallback
import com.example.sportify.utils.extensions.toFirebaseTimestamp

class FirebaseModel {
    private val database = Firebase.firestore

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }
        database.firestoreSettings = settings
    }

    fun getAllGames(sinceLastUpdated: Long, callback: GamesCallback) {
        database.collection(Constants.Collections.GAMES)
            .whereGreaterThanOrEqualTo(Game.LAST_UPDATED, sinceLastUpdated.toFirebaseTimestamp)
            .get()
            .addOnCompleteListener {
            when (it.isSuccessful) {
                true -> {
                    val games: MutableList<Game> = mutableListOf()
                    for (json in it.result) {
                        games.add(Game.fromJSON(json.data))
                    }
                    callback(games)
                }
                false -> callback(listOf())
            }
        }
    }

    fun getGameById(gameId: String, callback: (Game?) -> Unit) {
        database.collection(Constants.Collections.GAMES).document(gameId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val game = Game.fromJSON(document.data!!)
                        callback(game) // Successfully fetched the game
                    } else {
                        callback(null) // Document does not exist
                    }
                } else {
                    callback(null) // Task failed
                }
            }
    }

    fun addGame(game: Game, callback: EmptyCallback) {
        database.collection(Constants.Collections.GAMES).document(game.id).set(game.json)
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
            }
    }

    fun deleteGame(game: Game, callback: (Boolean) -> Unit) {
        database.collection(Constants.Collections.GAMES).document(game.id).delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true) // Successfully deleted the game
                } else {
                    callback(false) // Deletion failed
                }
            }
    }
}