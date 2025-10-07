package com.example.hydrasync.data

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.hydrasync.login.User
import kotlinx.coroutines.tasks.await

class UserRepository private constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val database: FirebaseDatabase =
        FirebaseDatabase.getInstance("https://hydrasync-14b0a-default-rtdb.asia-southeast1.firebasedatabase.app")

    // Get reference to current user's data
    fun getUserReference(): DatabaseReference? {
        val currentUser = firebaseAuth.currentUser
        return currentUser?.uid?.let { uid ->
            database.getReference("users").child(uid)  // Note: using "users" not "user_profiles"
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository().also { INSTANCE = it }
            }
        }
    }

    // Save user profile after registration
    @SuppressLint("RestrictedApi")
    suspend fun saveUserProfile(user: User): Boolean {
        return try {
            Log.d("UserRepository", "Attempting to save user profile for UID: ${user.uid}")
            val userRef = getUserReference()
            if (userRef == null) {
                Log.e("UserRepository", "getUserReference returned null - user might not be authenticated")
                return false
            }

            Log.d("UserRepository", "User reference obtained: ${userRef.path}")
            val profileData = mapOf(
                "firstName" to user.firstName,
                "lastName" to user.lastName,
                "email" to user.email,
                "gender" to user.gender,
                "birthday" to user.birthday,
                "createdAt" to System.currentTimeMillis()
            )

            userRef.child("profile").setValue(profileData).await()
            Log.d("UserRepository", "Profile saved successfully to Firebase")
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving profile: ${e.message}", e)
            false
        }
    }

    // Initialize default settings for new user
    suspend fun initializeDefaultSettings(): Boolean {
        return try {
            val userRef = getUserReference() ?: return false
            val defaultSettings = mapOf(
                "daily_goal_ml" to 2000,
                "inactivity_threshold" to 60,
                "quiet_hours_start" to "22:00",
                "quiet_hours_end" to "07:00"
            )
            userRef.child("settings").setValue(defaultSettings).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get user profile from Firebase
    suspend fun getUserProfile(): User? {
        return try {
            val userRef = getUserReference() ?: return null
            val snapshot = userRef.child("profile").get().await()

            if (snapshot.exists()) {
                User(
                    uid = firebaseAuth.currentUser?.uid ?: "",
                    email = snapshot.child("email").getValue(String::class.java) ?: "",
                    firstName = snapshot.child("firstName").getValue(String::class.java) ?: "",
                    lastName = snapshot.child("lastName").getValue(String::class.java) ?: "",
                    gender = snapshot.child("gender").getValue(String::class.java) ?: "",
                    birthday = snapshot.child("birthday").getValue(String::class.java) ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Get current user's UID
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // Update user profile
    suspend fun updateUserProfile(user: User): Boolean {
        return try {
            val userRef = getUserReference() ?: return false
            userRef.child("profile").setValue(mapOf(
                "firstName" to user.firstName,
                "lastName" to user.lastName,
                "email" to user.email,
                "gender" to user.gender,
                "birthday" to user.birthday,
                "updatedAt" to System.currentTimeMillis()
            )).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}