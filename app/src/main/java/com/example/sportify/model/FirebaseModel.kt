package com.example.sportify.model

import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase
import com.example.sportify.base.Constants
import com.example.sportify.base.EmptyCallback
import com.example.sportify.base.GamesCallback
import com.example.sportify.model.dao.User
import com.example.sportify.utils.extensions.toFirebaseTimestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.SetOptions

class FirebaseModel {
    private val database = Firebase.firestore
    private val TAG = "FirebaseModel"

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
                    false -> {
                        Log.e(TAG, "Error getting games: ${it.exception?.message}")
                        callback(listOf())
                    }
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
                    Log.e(TAG, "Error getting game: ${task.exception?.message}")
                    callback(null) // Task failed
                }
            }
    }

    fun addGame(game: Game, callback: EmptyCallback) {
        // Use merge option to prevent overwriting fields that might be set by other clients
        database.collection(Constants.Collections.GAMES).document(game.id).set(game.json, SetOptions.merge())
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding game: ${e.message}", e)
                // Still call callback to not block UI
                callback()
            }
    }

    fun deleteGame(game: Game, callback: (Boolean) -> Unit) {
        database.collection(Constants.Collections.GAMES).document(game.id).delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true) // Successfully deleted the game
                } else {
                    Log.e(TAG, "Error deleting game: ${task.exception?.message}")
                    callback(false) // Deletion failed
                }
            }
    }

    fun deleteWeatherFields(gameId: String, callback: EmptyCallback) {
        val updates = mapOf<String, Any?>(
            "weatherTemp" to null,
            "weatherDescription" to null,
            "weatherIcon" to null
        )

        database.collection(Constants.Collections.GAMES).document(gameId)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Weather fields deleted for game: $gameId")
                callback()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting weather fields: ${e.message}", e)
                // Still call callback to not block UI
                callback()
            }
    }


    fun listenForGameChanges(callback: (List<Game>, List<Game>) -> Unit) {
        database.collection(Constants.Collections.GAMES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to Game changes", error)
                    return@addSnapshotListener
                }

                val updatedGames = mutableListOf<Game>()
                val deletedGames = mutableListOf<Game>()

                snapshot?.documentChanges?.forEach { change ->
                    val game = Game.fromJSON(change.document.data)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            updatedGames.add(game)
                        }
                        DocumentChange.Type.REMOVED -> {
                            deletedGames.add(game)
                        }
                    }
                }

                callback(updatedGames, deletedGames)
            }
    }

    fun getUserById(userId: String, callback: (User?) -> Unit, erorrCallback: (String?) -> Unit) {
        database.collection(Constants.Collections.USERS).document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = User.fromMap(document.data ?: mapOf(), userId)
                    callback(user)
                } else {
                    Log.e(TAG, "User document doesn't exist")
                }
            }
            .addOnFailureListener { e ->
                erorrCallback(e.message)
            }
    }

    fun updateUser(userId: String, field: String, value: Any, callback: (String?) -> Unit) {
        database.collection(Constants.Collections.USERS).document(userId)
            .update(field, value)
            .addOnSuccessListener {
                Log.d(TAG, "User field $field updated successfully")
                callback("User field $field updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update profile: ${e.message}")
                callback("Failed to update profile: ${e.message}")
            }
    }
}