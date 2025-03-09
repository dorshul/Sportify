package com.example.sportify.model

import android.content.Context
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sportify.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

@Entity
data class Game(
    @PrimaryKey val id: String,
    val userId: String,
    val pictureUrl: String,
    val location: String,
    val description: String,
    val numberOfPlayers: Int,
    var approvals: Int,
    var isApproved: Boolean,
    val lastUpdated: Long? = null,
    var weatherTemp: String? = null,
    var weatherDescription: String? = null,
    var weatherIcon: String? = null

) {

    companion object {
        var lastUpdated: Long
            get() = MyApplication.Globals.context?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                ?.getLong(LOCAL_LAST_UPDATED, 0) ?: 0
            set(value) {
                MyApplication.Globals.context
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)?.apply {
                        edit().putLong(LOCAL_LAST_UPDATED, value).apply()
                    }
            }

        const val ID_KEY = "id"
        const val USER_ID_KEY = "userId"
        const val PICTURE_URL_KEY = "pictureUrl"
        const val LOCATION_KEY = "location"
        const val DESCRIPTION_KEY = "description"
        const val NUMBER_OF_PLAYERS_KEY = "numberOfPlayers"
        const val APPROVALS_KEY = "approvals"
        const val IS_APPROVED_KEY = "isApproved"
        const val LAST_UPDATED = "lastUpdated"
        const val LOCAL_LAST_UPDATED = "locaStudentLastUpdated"

        fun fromJSON(json: Map<String, Any>): Game {
            val id = json[ID_KEY] as? String ?: ""
            val userId = json[USER_ID_KEY] as? String ?: ""
            val pictureUrl = json[PICTURE_URL_KEY] as? String ?: ""
            val location = json[LOCATION_KEY] as? String ?: ""
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val numberOfPlayers = (json[NUMBER_OF_PLAYERS_KEY] as? Number)?.toInt() ?: 0
            val approvals = (json[APPROVALS_KEY] as? Number)?.toInt() ?: 0
            val isApproved = json[IS_APPROVED_KEY] as? Boolean ?: false
            val weatherTemp = json["weatherTemp"] as? String
            val weatherDescription = json["weatherDescription"] as? String
            val weatherIcon = json["weatherIcon"] as? String

            val timeStamp = json[LAST_UPDATED] as? Timestamp
            val lastUpdatedLongTimestamp = timeStamp?.toDate()?.time

            return Game(
                id = id,
                userId = userId,
                pictureUrl = pictureUrl,
                location = location,
                description = description,
                numberOfPlayers = numberOfPlayers,
                approvals = approvals,
                isApproved = isApproved,
                lastUpdated = lastUpdatedLongTimestamp,
                weatherTemp = weatherTemp,
                weatherDescription = weatherDescription,
                weatherIcon = weatherIcon
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
            IS_APPROVED_KEY to isApproved,
            LAST_UPDATED to FieldValue.serverTimestamp()
        )

}
