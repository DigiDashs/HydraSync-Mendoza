package com.example.hydrasync.login

data class User(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val birthday: String = ""
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "", "", "")

    fun getFullName(): String = "$firstName $lastName"
}