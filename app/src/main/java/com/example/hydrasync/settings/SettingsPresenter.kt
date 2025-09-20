package com.example.hydrasync.settings

import com.example.hydrasync.settings.data.SettingsData

class SettingsPresenter(
    private val view: SettingsContract.View,
    private val repository: SettingsContract.Repository = SettingsRepository()
) : SettingsContract.Presenter {

    private var settingsData: SettingsData? = null

    override fun loadSettingsData() {
        try {
            settingsData = repository.getSettingsData()
            settingsData?.let { data ->
                view.displaySettingsData(data)
            }
        } catch (e: Exception) {
            view.showToast("Error loading settings data")
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
        try {
            val success = repository.updateDailyGoal(goalML)
            if (success) {
                settingsData = settingsData?.copy(dailyGoalML = goalML)
                view.updateDailyGoal(goalML)
                view.showToast("Daily goal updated successfully")
            } else {
                view.showToast("Failed to update daily goal")
            }
        } catch (e: Exception) {
            view.showToast("Error updating daily goal")
        }
    }

    override fun updateInactivityAlert(minutes: Int) {
        try {
            val success = repository.updateInactivityAlert(minutes)
            if (success) {
                settingsData = settingsData?.copy(inactivityAlertMinutes = minutes)
                view.updateInactivityAlert(minutes)
                view.showToast("Inactivity alert updated successfully")
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
                view.showToast("Quiet hours updated successfully")
            } else {
                view.showToast("Failed to update quiet hours")
            }
        } catch (e: Exception) {
            view.showToast("Error updating quiet hours")
        }
    }

    override fun onDestroy() {
        // Clean up any resources if needed
        settingsData = null
    }
}