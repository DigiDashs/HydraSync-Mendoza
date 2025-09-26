package com.example.hydrasync.login

data class LoginResponse(
    val isSuccess: Boolean,
    val message: String,
    val user: User? = null
)