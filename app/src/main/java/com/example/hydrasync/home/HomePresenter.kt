package com.example.hydrasync.home

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.example.hydrasync.data.WaterIntakeRepository
import com.example.hydrasync.login.LoginRepository
import com.example.hydrasync.settings.SettingsRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class HomePresenter(private var view: HomeContract.View?) : HomeContract.Presenter {

    private val loginRepository = LoginRepository.getInstance()
    private val waterRepository = WaterIntakeRepository.getInstance()
    private val settingsRepository = SettingsRepository()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://hydrasync-14b0a-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var hasShownGoalAchievement = false

    override fun loadHomeData() {
        scope.launch {
            val currentUser = loginRepository.getCurrentUser()
            Log.d("HydraSync", "loadHomeData() called - currentUser: $currentUser")

            if (currentUser != null) {
                try {
                    // Load daily goal from Firebase
                    val dailyGoal = withContext(Dispatchers.IO) {
                        settingsRepository.getDailyGoal()
                    }

                    // Update repository with the goal
                    waterRepository.setDailyGoal(dailyGoal)
                    Log.d("HydraSync", "Daily goal loaded: $dailyGoal")

                    // Add sample data for testing (only if empty)
                    waterRepository.addSampleData()

                    val waterIntake = waterRepository.getCurrentWaterIntake()
                    val homeData = HomeData(
                        user = currentUser,
                        waterIntake = waterIntake,
                        isConnected = true,
                        goalAchievedToday = waterIntake.isGoalAchieved()
                    )

                    view?.displayHomeData(homeData)
                    view?.updateWaterProgress(waterIntake)
                    updateProgressView()

                } catch (e: Exception) {
                    Log.e("HydraSync", "Error loading settings", e)
                    view?.showToast("Error loading settings, using default goal")

                    val waterIntake = waterRepository.getCurrentWaterIntake()
                    val homeData = HomeData(
                        user = currentUser,
                        waterIntake = waterIntake,
                        isConnected = true,
                        goalAchievedToday = waterIntake.isGoalAchieved()
                    )
                    view?.displayHomeData(homeData)
                    view?.updateWaterProgress(waterIntake)
                    updateProgressView()
                }
            } else {
                Log.w("HydraSync", "User is null - navigating to login")
                view?.navigateToLogin()
            }
        }
    }

    override fun onAddIntakeClicked() {
        Log.d("HydraSync", "Add intake button clicked")
        view?.showAddIntakeDialog()
    }

    override fun addWaterIntake(amount: Int) {
        Log.d("HydraSync", "addWaterIntake() called with amount: $amount")

        val currentIntake = waterRepository.getCurrentWaterIntake()
        val wasGoalAchieved = currentIntake.isGoalAchieved()

        // Add to local repository (for UI)
        waterRepository.addDrinkEntry(amount)
        Log.d("HydraSync", "Local repository updated with $amount ml")

        val updatedIntake = waterRepository.getCurrentWaterIntake()

        // ✅ Save data to Firebase Realtime Database
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("HydraSync", "Attempting to save to Firebase for user: $userId")

            val ref = database.getReference("users").child(userId).child("waterLogs")

            val logEntry = mapOf(
                "amount" to amount,
                "timestamp" to System.currentTimeMillis(),
                "date" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )

            ref.push().setValue(logEntry)
                .addOnSuccessListener {
                    Log.d("HydraSync", "✅ Successfully saved drink log to Firebase for user: $userId")
                    view?.showToast("Saved to cloud!")
                }
                .addOnFailureListener { e ->
                    Log.e("HydraSync", "❌ Failed to save drink log to Firebase", e)
                    view?.showError("Failed to save: ${e.message}")
                }
        } else {
            Log.e("HydraSync", "❌ User not logged in - cannot save to Firebase")
            view?.showError("User not logged in.")
        }

        // ✅ Goal achievement logic (same as before)
        if (!wasGoalAchieved && updatedIntake.isGoalAchieved() && !hasShownGoalAchievement) {
            Log.d("HydraSync", "🎉 Goal achieved for today!")
            view?.showGoalAchieved()
            hasShownGoalAchievement = true
        }

        view?.updateWaterProgress(updatedIntake)
        updateProgressView()
        view?.showToast("Added ${amount}ml - ${updatedIntake.getStatusText()}")
    }

    override fun setDailyGoal(goal: Int) {
        view?.showToast("Please use Settings to change your daily goal")
    }

    override fun resetDailyProgress() {
        Log.d("HydraSync", "Daily progress reset called")
        waterRepository.resetDailyProgress()
        hasShownGoalAchievement = false
        val updatedIntake = waterRepository.getCurrentWaterIntake()
        view?.updateWaterProgress(updatedIntake)
        updateProgressView()
        view?.showToast("Daily progress reset - new day started!")
    }

    private fun updateProgressView() {
        val currentIntake = waterRepository.getCurrentWaterIntake()
        view?.showGoalProgress(
            currentIntake.getPercentage(),
            currentIntake.isGoalAchieved()
        )
    }

    override fun onHistoryClicked() {
        Log.d("HydraSync", "History clicked")
        view?.navigateToHistory()
    }

    override fun onSettingsClicked() {
        Log.d("HydraSync", "Settings clicked")
        view?.navigateToSettings()
    }

    override fun onLogoutClicked() {
        Log.d("HydraSync", "Logout clicked")
        loginRepository.logout()
        view?.navigateToLogin()
    }

    override fun onDestroy() {
        Log.d("HydraSync", "Presenter destroyed")
        scope.cancel()
        view = null
    }
}
