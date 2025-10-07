package com.example.hydrasync.profile
data class Profile(
    val firstName: String,  // Change from name
    val lastName: String,   // Add this
    val email: String,
    val gender: String,
    val birthday: String
) {
    // Helper to get full name for display
    fun getFullName(): String = "$firstName $lastName"
}
