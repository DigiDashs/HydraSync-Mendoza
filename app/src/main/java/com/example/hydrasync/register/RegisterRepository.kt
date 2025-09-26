package com.example.hydrasync.register

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class RegisterRepository private constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var INSTANCE: RegisterRepository? = null

        fun getInstance(): RegisterRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RegisterRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun register(firstName: String, lastName: String, email: String, password: String): RegisterResponse {
        return try {
            // Create user with email and password
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Update user profile with first and last name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName("$firstName $lastName")
                    .build()

                firebaseUser.updateProfile(profileUpdates).await()

                // Convert FirebaseUser to our User model
                val user = com.example.hydrasync.login.User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    firstName = firstName,
                    lastName = lastName
                )

                RegisterResponse(true, "Registration successful!", user)
            } else {
                RegisterResponse(false, "Registration failed - no user created")
            }
        } catch (e: Exception) {
            RegisterResponse(false, getFirebaseErrorMessage(e))
        }
    }

    private fun getFirebaseErrorMessage(exception: Exception): String {
        return when (exception.message) {
            "The email address is badly formatted." -> "Invalid email format"
            "The email address is already in use by another account." -> "Email already registered"
            "The given password is invalid. [ Password should be at least 6 characters ]" -> "Password must be at least 6 characters"
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Network error. Please check your connection"
            else -> "Registration failed: ${exception.message}"
        }
    }

    fun getCurrentUser(): com.example.hydrasync.login.User? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let { user ->
            val names = user.displayName?.split(" ") ?: listOf("", "")
            com.example.hydrasync.login.User(
                uid = user.uid,
                email = user.email ?: "",
                firstName = names.getOrNull(0) ?: "",
                lastName = names.getOrNull(1) ?: ""
            )
        }
    }
}