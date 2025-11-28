package com.example.hydrasync.settings

import com.example.hydrasync.alerts.InactivityManager
import com.example.hydrasync.settings.data.SettingsData
import kotlinx.coroutines.*

class SettingsPresenter(
    private val view: SettingsContract.View,
    private val repository: SettingsRepository = SettingsRepository()
) : SettingsContract.Presenter {

    private var settingsData: SettingsData? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        const val MIN_GOAL = 500
        const val MAX_GOAL = 5000
        const val DEFAULT_GOAL = 2000
    }

    override fun loadSettingsData() {
        scope.launch {
            try {
                settingsData = withContext(Dispatchers.IO) {
                    repository.getSettingsDataAsync()
                }
                settingsData?.let { data ->
                    view.displaySettingsData(data)
                }
            } catch (e: Exception) {
                view.showToast("Error loading settings data")
            }
        }
    }

    override fun onProfileClicked() {
        view.navigateToProfile()
    }

    override fun onDailyGoalClicked() {
        settingsData?.let { data ->
            view.showDailyGoalDialog(data.dailyGoalML)
        }
    }

    override fun onInactivityAlertClicked() {
        settingsData?.let { data ->
            view.showInactivityAlertDialog(data.inactivityAlertMinutes)
        }
    }

    override fun onQuietHoursClicked() {
        settingsData?.let { data ->
            view.showQuietHoursDialog(data.quietHoursStart, data.quietHoursEnd)
        }
    }

    override fun onLogoutClicked() {
        view.showLogoutConfirmationDialog()
    }

    override fun onLogoutConfirmed() {
        try {
            val success = repository.logout()
            if (success) {
                view.navigateToLogin()
            } else {
                view.showToast("Logout failed. Please try again.")
            }
        } catch (e: Exception) {
            view.showToast("Error during logout")
        }
    }

    override fun onHistoryClicked() {
        view.navigateToHistory()
    }

    override fun onHomeClicked() {
        view.navigateToHome()
    }

    override fun updateDailyGoal(goalML: Int) {
        // Validate goal
        if (goalML < MIN_GOAL || goalML > MAX_GOAL) {
            view.showToast("Goal must be between $MIN_GOAL mL and $MAX_GOAL mL")
            return
        }

        scope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    repository.updateDailyGoalAsync(goalML)
                }
                if (success) {
                    settingsData = settingsData?.copy(dailyGoalML = goalML)
                    view.updateDailyGoal(goalML)
                    view.showToast("Daily goal updated to $goalML mL")
                } else {
                    view.showToast("Failed to update daily goal")
                }
            } catch (e: Exception) {
                view.showToast("Error updating daily goal: ${e.message}")
            }
        }
    }

    override fun updateInactivityAlert(minutes: Int) {
        try {
            val success = repository.updateInactivityAlert(minutes)
            if (success) {
                settingsData = settingsData?.copy(inactivityAlertMinutes = minutes)
                view.updateInactivityAlert(minutes)

                val context = (view as? android.content.Context)
                context?.let {
                    val manager = InactivityManager(it)
                    manager.cancelInactivityCheck()

                    if (minutes > 0) {
                        manager.scheduleInactivityCheck(minutes)
                    }
                }

                view.showToast(
                    if (minutes == 0) "Inactivity alerts disabled"
                    else "Inactivity alert updated to $minutes mins"
                )
            } else {
                view.showToast("Failed to update inactivity alert")
            }
        } catch (e: Exception) {
            view.showToast("Error updating inactivity alert")
        }
    }

    override fun updateQuietHours(startTime: String, endTime: String) {
        try {
            val success = repository.updateQuietHours(startTime, endTime)
            if (success) {
                settingsData = settingsData?.copy(
                    quietHoursStart = startTime,
                    quietHoursEnd = endTime
                )
                view.updateQuietHours(startTime, endTime)
                view.showToast("Quiet hours updated to $startTime-$endTime")
            } else {
                view.showToast("Failed to update quiet hours")
            }
        } catch (e: Exception) {
            view.showToast("Error updating quiet hours")
        }
    }

    override fun onEsp32SetupClicked() {
        view.navigateToEsp32Setup()
    }


    override fun onDestroy() {
        scope.cancel()
        settingsData = null
    }
}