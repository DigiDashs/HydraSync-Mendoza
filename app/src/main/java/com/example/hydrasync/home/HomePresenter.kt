package com.example.hydrasync.home

import android.util.Log
import com.example.hydrasync.data.WaterIntakeRepository
import com.example.hydrasync.data.WaterLog
import com.example.hydrasync.login.LoginRepository
import com.example.hydrasync.settings.SettingsRepository
import kotlinx.coroutines.*

class HomePresenter(private var view: HomeContract.View?) : HomeContract.Presenter {

    private val loginRepository = LoginRepository.getInstance()
    private val waterRepository = WaterIntakeRepository.getInstance()
    private val settingsRepository = SettingsRepository()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var hasShownGoalAchievement = false
    private var dailyGoal: Int = 2000 // Default goal

    override fun loadHomeData() {
        scope.launch {
            val currentUser = loginRepository.getCurrentUser()
            Log.d("HydraSync", "loadHomeData() called - currentUser: $currentUser")

            if (currentUser != null) {
                try {
                    // Load daily goal from Firebase using SettingsRepository
                    dailyGoal = withContext(Dispatchers.IO) {
                        settingsRepository.getDailyGoal()
                    }

                    Log.d("HydraSync", "Daily goal loaded: $dailyGoal")

                    // Get today's water intake from Firebase
                    val todayTotalIntake = withContext(Dispatchers.IO) {
                        waterRepository.getTodayTotalIntake()
                    }

                    // Get today's water logs for last drink info
                    val todayLogs = withContext(Dispatchers.IO) {
                        waterRepository.getTodayWaterLogs()
                    }

                    val lastDrink = if (todayLogs.isNotEmpty()) {
                        val latestLog = todayLogs.maxByOrNull { it.timestamp }
                        "${latestLog?.amount ?: 0} ML"
                    } else {
                        "0 ML"
                    }

                    val lastDrinkTime = if (todayLogs.isNotEmpty()) {
                        todayLogs.maxByOrNull { it.timestamp }?.timestamp ?: 0L
                    } else {
                        0L
                    }

                    val waterIntake = WaterIntake(
                        currentIntake = todayTotalIntake,
                        dailyGoal = dailyGoal,
                        lastDrink = lastDrink,
                        lastDrinkTime = lastDrinkTime
                    )

                    val homeData = HomeData(
                        user = currentUser,
                        waterIntake = waterIntake,
                        isConnected = true,
                        goalAchievedToday = waterIntake.isGoalAchieved()
                    )

                    view?.displayHomeData(homeData)
                    view?.updateWaterProgress(waterIntake)
                    updateProgressView()

                    // Check for goal achievement
                    if (waterIntake.isGoalAchieved() && !hasShownGoalAchievement) {
                        Log.d("HydraSync", "🎉 Goal achieved for today!")
                        view?.showGoalAchieved()
                        hasShownGoalAchievement = true
                    }

                } catch (e: Exception) {
                    Log.e("HydraSync", "Error loading data: ${e.message}", e)
                    view?.showToast("Error loading data, using default values")

                    // Fallback with default values
                    val waterIntake = WaterIntake(
                        currentIntake = 0,
                        dailyGoal = dailyGoal,
                        lastDrink = "0 ML",
                        lastDrinkTime = 0L
                    )

                    val homeData = HomeData(
                        user = currentUser,
                        waterIntake = waterIntake,
                        isConnected = true,
                        goalAchievedToday = false
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

        scope.launch {
            try {
                // Get current intake before adding
                val currentIntake = withContext(Dispatchers.IO) {
                    waterRepository.getTodayTotalIntake()
                }
                val wasGoalAchieved = currentIntake >= dailyGoal

                // Save to Firebase using the repository
                val success = withContext(Dispatchers.IO) {
                    waterRepository.addDrinkEntry(amount)
                }

                if (success) {
                    Log.d("HydraSync", "✅ Successfully saved drink log to Firebase")

                    // Reload data to get updated values
                    val newTotalIntake = withContext(Dispatchers.IO) {
                        waterRepository.getTodayTotalIntake()
                    }

                    val todayLogs = withContext(Dispatchers.IO) {
                        waterRepository.getTodayWaterLogs()
                    }

                    val lastDrink = if (todayLogs.isNotEmpty()) {
                        val latestLog = todayLogs.maxByOrNull { it.timestamp }
                        "${latestLog?.amount ?: 0} ML"
                    } else {
                        "0 ML"
                    }

                    val lastDrinkTime = if (todayLogs.isNotEmpty()) {
                        todayLogs.maxByOrNull { it.timestamp }?.timestamp ?: 0L
                    } else {
                        0L
                    }

                    val updatedIntake = WaterIntake(
                        currentIntake = newTotalIntake,
                        dailyGoal = dailyGoal,
                        lastDrink = lastDrink,
                        lastDrinkTime = lastDrinkTime
                    )

                    // Goal achievement logic
                    if (!wasGoalAchieved && updatedIntake.isGoalAchieved() && !hasShownGoalAchievement) {
                        Log.d("HydraSync", "🎉 Goal achieved for today!")
                        view?.showGoalAchieved()
                        hasShownGoalAchievement = true
                    }

                    view?.updateWaterProgress(updatedIntake)
                    updateProgressView()
                    view?.showToast("Added ${amount}ml - ${updatedIntake.getStatusText()}")
                } else {
                    Log.e("HydraSync", "❌ Failed to save drink log to Firebase")
                    view?.showError("Failed to save to cloud")
                }

            } catch (e: Exception) {
                Log.e("HydraSync", "❌ Error saving to Firebase", e)
                view?.showError("Error saving: ${e.message}")
            }
        }
    }

    override fun setDailyGoal(goal: Int) {
        view?.showToast("Please use Settings to change your daily goal")
    }

    override fun resetDailyProgress() {
        Log.d("HydraSync", "Daily progress reset called")
        // Note: We don't reset daily progress in Firebase since we want to keep historical data
        // Instead, we just reset the local achievement flag and reload data
        hasShownGoalAchievement = false

        scope.launch {
            try {
                // Reload current data to refresh the UI
                loadHomeData()
                view?.showToast("Progress refreshed!")

            } catch (e: Exception) {
                Log.e("HydraSync", "Error resetting progress: ${e.message}", e)
                view?.showError("Error refreshing data")
            }
        }
    }

    private fun updateProgressView() {
        scope.launch {
            try {
                val todayTotalIntake = withContext(Dispatchers.IO) {
                    waterRepository.getTodayTotalIntake()
                }

                val waterIntake = WaterIntake(
                    currentIntake = todayTotalIntake,
                    dailyGoal = dailyGoal,
                    lastDrinkTime = System.currentTimeMillis()
                )

                view?.showGoalProgress(
                    waterIntake.getPercentage(),
                    waterIntake.isGoalAchieved()
                )
            } catch (e: Exception) {
                Log.e("HydraSync", "Error updating progress view: ${e.message}", e)
            }
        }
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
