package com.example.hydrasync.settings

import com.example.hydrasync.settings.data.SettingsData

interface SettingsContract {

    interface View {
        fun displaySettingsData(settingsData: SettingsData)
        fun updateDailyGoal(goalML: Int)
        fun updateInactivityAlert(minutes: Int)
        fun updateQuietHours(startTime: String, endTime: String)
        fun showDailyGoalDialog(currentGoal: Int)
        fun showInactivityAlertDialog(currentMinutes: Int)
        fun showQuietHoursDialog(startTime: String, endTime: String)
        fun showLogoutConfirmationDialog()
        fun navigateToLogin()
        fun navigateToProfile()
        fun navigateToHome()
        fun navigateToHistory()
        fun showToast(message: String)
        fun navigateToEsp32Setup()

    }

    interface Presenter {
        fun loadSettingsData()
        fun onProfileClicked()
        fun onDailyGoalClicked()
        fun onInactivityAlertClicked()
        fun onQuietHoursClicked()
        fun onLogoutClicked()
        fun onLogoutConfirmed()
        fun onHistoryClicked()
        fun onHomeClicked()
        fun updateDailyGoal(goalML: Int)
        fun updateInactivityAlert(minutes: Int)
        fun updateQuietHours(startTime: String, endTime: String)
        fun onEsp32SetupClicked()

        fun onDestroy()
    }

    interface Repository {
        fun getSettingsData(): SettingsData
        fun updateDailyGoal(goalML: Int): Boolean
        fun updateInactivityAlert(minutes: Int): Boolean
        fun updateQuietHours(startTime: String, endTime: String): Boolean
        fun logout(): Boolean
    }
}