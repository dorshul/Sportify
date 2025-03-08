package com.example.sportify.model.dao

data class User(
    val id: String,
    val name: String,
    val age: Int,
    val profileImageUrl: String = ""
) {
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "age" to age,
            "profileImageUrl" to profileImageUrl
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>, userId: String): User {
            return User(
                id = userId,
                name = map["name"] as? String ?: "",
                age = (map["age"] as? Long)?.toInt() ?: 0,
                profileImageUrl = map["profileImageUrl"] as? String ?: ""
            )
        }
    }
}