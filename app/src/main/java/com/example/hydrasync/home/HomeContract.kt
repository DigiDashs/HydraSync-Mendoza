package com.example.hydrasync.home

import com.example.hydrasync.login.User

interface HomeContract {
    interface View {
        fun displayHomeData(homeData: HomeData)
        fun showAddIntakeDialog()
        fun updateWaterProgress(intake: WaterIntake)
        fun navigateToLogin()
        fun navigateToHistory()
        fun navigateToSettings()
    }

    interface Presenter {
        fun loadHomeData()
        fun onAddIntakeClicked()
        fun addWaterIntake(amount: Int)
        fun onHistoryClicked()
        fun onSettingsClicked()
        fun onLogoutClicked()
        fun onDestroy()
    }
}