package com.example.sportify.model

import android.util.Log
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
) {

    companion object {
        const val ID_KEY = "id"
        const val USER_ID_KEY = "userId"
        const val PICTURE_URL_KEY = "pictureUrl"
        const val LOCATION_KEY = "location"
        const val DESCRIPTION_KEY = "description"
        const val NUMBER_OF_PLAYERS_KEY = "numberOfPlayers"
        const val APPROVALS_KEY = "approvals"
        const val IS_APPROVED_KEY = "isApproved"

        fun fromJSON(json: Map<String, Any>): Game {
            val id = json[ID_KEY] as? String ?: ""
            val userId = json[USER_ID_KEY] as? String ?: ""
            val pictureUrl = json[PICTURE_URL_KEY] as? String ?: ""
            val location = json[LOCATION_KEY] as? String ?: ""
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val numberOfPlayers = (json[NUMBER_OF_PLAYERS_KEY] as? Number)?.toInt() ?: 0
            val approvals = (json[APPROVALS_KEY] as? Number)?.toInt() ?: 0
            val isApproved = json[IS_APPROVED_KEY] as? Boolean ?: false

            return Game(
                id = id,
                userId = userId,
                pictureUrl = pictureUrl,
                location = location,
                description = description,
                numberOfPlayers = numberOfPlayers,
                approvals = approvals,
                isApproved = isApproved
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            USER_ID_KEY to userId,
            PICTURE_URL_KEY to pictureUrl,
            LOCATION_KEY to location,
            DESCRIPTION_KEY to description,
            NUMBER_OF_PLAYERS_KEY to numberOfPlayers,
            APPROVALS_KEY to approvals,
            IS_APPROVED_KEY to isApproved
        )

}
