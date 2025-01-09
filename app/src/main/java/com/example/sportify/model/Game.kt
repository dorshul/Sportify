package com.example.sportify.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Game(
    @PrimaryKey val id: String,
    val userId: String,
    val pictureUrl: String,
    val location: String,
    val description: String,
    val numberOfPlayers: Int,
    var approvals: Int,
    var isApproved: Boolean
)
