package com.example.hydrasync.register

import com.example.hydrasync.login.User

data class RegisterResponse(
    val isSuccess: Boolean,
    val message: String,
    val user: User? = null
)