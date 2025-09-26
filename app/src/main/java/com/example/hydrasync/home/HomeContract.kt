package com.example.hydrasync.home

import com.example.hydrasync.login.User

interface HomeContract {
    interface View {
        fun displayHomeData(homeData: HomeData)
        fun showAddIntakeDialog()
        fun updateWaterProgress(intake: WaterIntake)
        fun showGoalAchieved() // New method for goal celebration
        fun showGoalProgress(percentage: Int, isGoalAchieved: Boolean) // Enhanced progress
        fun navigateToLogin()
        fun navigateToHistory()
        fun navigateToSettings()
        fun showError(message: String)
        fun showToast(message: String)
    }

    interface Presenter {
        fun loadHomeData()
        fun onAddIntakeClicked()
        fun addWaterIntake(amount: Int)
        fun setDailyGoal(goal: Int) // New method for setting goals
        fun resetDailyProgress() // New method for daily reset
        fun onHistoryClicked()
        fun onSettingsClicked()
        fun onLogoutClicked()
        fun onDestroy()
    }
}
