package com.example.hydrasync.login

data class LoginUser(
    val email: String,
    val password: String
)

data class LoginResponse(
    val isSuccess: Boolean,
    val message: String,
    val user: User? = null
)