package com.example.hydrasync.settings

import android.util.Log
import com.example.hydrasync.login.LoginRepository
import com.example.hydrasync.settings.data.SettingsData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class SettingsRepository : SettingsContract.Repository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://hydrasync-14b0a-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
    private val loginRepository = LoginRepository.getInstance()

    private fun getUserSettingsRef() = firebaseAuth.currentUser?.uid?.let { uid ->
        database.getReference("users").child(uid).child("settings")
    }

    private fun getUserProfileRef() = firebaseAuth.currentUser?.uid?.let { uid ->
        database.getReference("users").child(uid).child("profile")
    }

    override fun getSettingsData(): SettingsData {
        return SettingsData.getDefault()
    }

    suspend fun getSettingsDataAsync(): SettingsData {
        return try {
            val settingsRef = getUserSettingsRef() ?: return SettingsData.getDefault()
            val profileRef = getUserProfileRef() ?: return SettingsData.getDefault()

            val settingsSnapshot = settingsRef.get().await()
            val profileSnapshot = profileRef.get().await()

            val dailyGoal = settingsSnapshot.child("daily_goal_ml").getValue(Int::class.java) ?: 2000
            val inactivityThreshold = settingsSnapshot.child("inactivity_threshold").getValue(Int::class.java) ?: 60
            val quietHoursStart = settingsSnapshot.child("quiet_hours_start").getValue(String::class.java) ?: "10PM"
            val quietHoursEnd = settingsSnapshot.child("quiet_hours_end").getValue(String::class.java) ?: "7AM"

            val firstName = profileSnapshot.child("firstName").getValue(String::class.java) ?: ""
            val lastName = profileSnapshot.child("lastName").getValue(String::class.java) ?: ""
            val email = profileSnapshot.child("email").getValue(String::class.java) ?: ""

            SettingsData(
                dailyGoalML = dailyGoal,
                inactivityAlertMinutes = inactivityThreshold,
                quietHoursStart = quietHoursStart,
                quietHoursEnd = quietHoursEnd,
                userName = "$firstName $lastName",
                userEmail = email
            )
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Error loading settings: ${e.message}")
            SettingsData.getDefault()
        }
    }

    override fun updateDailyGoal(goalML: Int): Boolean {
        return try {
            val settingsRef = getUserSettingsRef() ?: return false
            settingsRef.child("daily_goal_ml").setValue(goalML)
            Log.d("SettingsRepository", "Daily goal updated to $goalML")
            true
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Error updating daily goal: ${e.message}")
            false
        }
    }

    suspend fun updateDailyGoalAsync(goalML: Int): Boolean {
        return try {
            val settingsRef = getUserSettingsRef() ?: return false
            settingsRef.child("daily_goal_ml").setValue(goalML).await()
            Log.d("SettingsRepository", "Daily goal updated to $goalML")
            true
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Error updating daily goal: ${e.message}")
            false
        }
    }

    override fun updateInactivityAlert(minutes: Int): Boolean {
        return try {
            val settingsRef = getUserSettingsRef() ?: return false
            settingsRef.child("inactivity_threshold").setValue(minutes)
            true
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Error updating inactivity alert: ${e.message}")
            false
        }
    }

    override fun updateQuietHours(startTime: String, endTime: String): Boolean {
        return try {
            val settingsRef = getUserSettingsRef() ?: return false
            settingsRef.child("quiet_hours_start").setValue(startTime)
            settingsRef.child("quiet_hours_end").setValue(endTime)
            true
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Error updating quiet hours: ${e.message}")
            false
        }
    }

    override fun logout(): Boolean {
        return try {
            // Clear the ESP32 pairing in Firebase
            val deviceRef = database.getReference("pairing").child("ESP32_001")
            deviceRef.child("activeUser").setValue("").addOnCompleteListener {
                Log.d("SettingsRepository", "✅ Active user cleared from /pairing/ESP32_001")
            }

            // Then log out from Firebase Auth
            loginRepository.logout()
            true
        } catch (e: Exception) {
            Log.e("SettingsRepository", "❌ Error during logout: ${e.message}")
            false
        }
    }


    suspend fun getDailyGoal(): Int {
        return try {
            val settingsRef = getUserSettingsRef() ?: return 2000
            val snapshot = settingsRef.child("daily_goal_ml").get().await()
            snapshot.getValue(Int::class.java) ?: 2000
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Error getting daily goal: ${e.message}")
            2000 // Return default goal on error
        }
    }
}