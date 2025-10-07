package com.example.hydrasync.register

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
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

    suspend fun register(firstName: String, lastName: String, email: String, password: String, gender: String, birthday: String): RegisterResponse {
        return try {
            Log.d("RegisterRepository", "Starting registration for: $email")
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                Log.d("RegisterRepository", "Firebase Auth user created: ${firebaseUser.uid}")

                // Update user profile with first and last name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName("$firstName $lastName")
                    .build()

                firebaseUser.updateProfile(profileUpdates).await()
                Log.d("RegisterRepository", "Firebase profile updated")

                // Convert FirebaseUser to our User model
                val user = com.example.hydrasync.login.User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    firstName = firstName,
                    lastName = lastName,
                    gender = gender,
                    birthday = birthday
                )

                Log.d("RegisterRepository", "User model created, saving to UserRepository")

                // RE-ENABLE UserRepository calls
                val userRepository = com.example.hydrasync.data.UserRepository.getInstance()
                val profileSaved = userRepository.saveUserProfile(user)
                val settingsInitialized = userRepository.initializeDefaultSettings()

                if (profileSaved && settingsInitialized) {
                    Log.d("RegisterRepository", "UserRepository operations successful")
                    RegisterResponse(true, "Registration successful!", user)
                } else {
                    Log.e("RegisterRepository", "UserRepository operations failed")
                    // If database save fails, delete the auth user (optional)
                    firebaseUser.delete().await()
                    RegisterResponse(false, "Failed to save user data")
                }
            } else {
                Log.e("RegisterRepository", "Firebase user is null")
                RegisterResponse(false, "Registration failed - no user created")
            }
        } catch (e: Exception) {
            Log.e("RegisterRepository", "Registration error: ${e.message}", e)
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