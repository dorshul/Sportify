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
    var approvals: MutableList<String>,
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
        const val LAST_UPDATED = "lastUpdated"
        const val LOCAL_LAST_UPDATED = "locaStudentLastUpdated"
        const val WEATHER_TEMP = "weatherTemp"
        const val WEATHER_DESCRIPTION = "weatherDescription"
        const val WEATHER_ICON = "weatherIcon"

        fun fromJSON(json: Map<String, Any>): Game {
            val id = json[ID_KEY] as? String ?: ""
            val userId = json[USER_ID_KEY] as? String ?: ""
            val pictureUrl = json[PICTURE_URL_KEY] as? String ?: ""
            val location = json[LOCATION_KEY] as? String ?: ""
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val numberOfPlayers = (json[NUMBER_OF_PLAYERS_KEY] as? Number)?.toInt() ?: 0
            val approvals = (json[APPROVALS_KEY] as? MutableList<String>) ?: mutableListOf()
            val weatherTemp = json[WEATHER_TEMP] as? String
            val weatherDescription = json[WEATHER_DESCRIPTION] as? String
            val weatherIcon = json[WEATHER_ICON] as? String

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
                lastUpdated = lastUpdatedLongTimestamp,
                weatherTemp = weatherTemp,
                weatherDescription = weatherDescription,
                weatherIcon = weatherIcon
            )
        }
    }

    val json: Map<String, Any>
        get() {
            val map = hashMapOf(
                ID_KEY to id,
                USER_ID_KEY to userId,
                PICTURE_URL_KEY to pictureUrl,
                LOCATION_KEY to location,
                DESCRIPTION_KEY to description,
                NUMBER_OF_PLAYERS_KEY to numberOfPlayers,
                APPROVALS_KEY to approvals,
                LAST_UPDATED to FieldValue.serverTimestamp()
            )

            // Add weather data if available
            weatherTemp?.let { map[WEATHER_TEMP] = it }
            weatherDescription?.let { map[WEATHER_DESCRIPTION] = it }
            weatherIcon?.let { map[WEATHER_ICON] = it }

            return map
        }

    // Override equals for more precise comparisons
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Game

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (pictureUrl != other.pictureUrl) return false
        if (location != other.location) return false
        if (description != other.description) return false
        if (numberOfPlayers != other.numberOfPlayers) return false
        if (approvals != other.approvals) return false
        if (weatherTemp != other.weatherTemp) return false
        if (weatherDescription != other.weatherDescription) return false
        if (weatherIcon != other.weatherIcon) return false

        return true
    }

    // Override hashCode to match our equals implementation
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + pictureUrl.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + numberOfPlayers
        result = 31 * result + approvals.hashCode()
        result = 31 * result + (weatherTemp?.hashCode() ?: 0)
        result = 31 * result + (weatherDescription?.hashCode() ?: 0)
        result = 31 * result + (weatherIcon?.hashCode() ?: 0)
        return result
    }
}