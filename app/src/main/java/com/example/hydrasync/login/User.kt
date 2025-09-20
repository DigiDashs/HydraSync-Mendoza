package com.example.hydrasync.login

data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String
) {
    fun getFullName(): String = "$firstName $lastName"
}