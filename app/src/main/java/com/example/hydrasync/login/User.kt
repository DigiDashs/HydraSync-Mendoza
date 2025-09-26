package com.example.hydrasync.login

data class User(
    val uid: String,  // This will store Firebase UID
    val email: String,
    val firstName: String,
    val lastName: String
) {
    fun getFullName(): String = "$firstName $lastName"
}