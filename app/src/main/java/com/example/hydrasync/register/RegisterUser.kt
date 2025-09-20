package com.example.hydrasync.register

data class RegisterUser(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val isSuccess: Boolean,
    val message: String
)