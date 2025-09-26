package com.example.hydrasync.home

import com.example.hydrasync.data.WaterIntakeRepository
import com.example.hydrasync.login.LoginRepository
import kotlinx.coroutines.*

class HomePresenter(private var view: HomeContract.View?) : HomeContract.Presenter {

    private val loginRepository = LoginRepository.getInstance()
    private val waterRepository = WaterIntakeRepository.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var hasShownGoalAchievement = false

    override fun loadHomeData() {
        val currentUser = loginRepository.getCurrentUser()

        if (currentUser != null) {
            // Add sample data for testing
            waterRepository.addSampleData()

            val waterIntake = waterRepository.getCurrentWaterIntake()
            val homeData = HomeData(
                user = currentUser,
                waterIntake = waterIntake,
                isConnected = true
            )
            view?.displayHomeData(homeData)
            view?.updateWaterProgress(waterIntake)
            updateProgressView()
        } else {
            view?.navigateToLogin()
        }
    }

    override fun onAddIntakeClicked() {
        val currentIntake = waterRepository.getCurrentWaterIntake()
        if (currentIntake.isGoalAchieved() && !hasShownGoalAchievement) {
            view?.showToast("Daily goal already achieved! Great job!")
        }
        view?.showAddIntakeDialog()
    }

    override fun addWaterIntake(amount: Int) {
        val currentIntake = waterRepository.getCurrentWaterIntake()
        val wasGoalAchieved = currentIntake.isGoalAchieved()

        // Add to repository (this will be reflected in History automatically)
        waterRepository.addDrinkEntry(amount)

        // Get updated intake status
        val updatedIntake = waterRepository.getCurrentWaterIntake()

        // Check if goal was just achieved
        if (!wasGoalAchieved && updatedIntake.isGoalAchieved() && !hasShownGoalAchievement) {
            view?.showGoalAchieved()
            hasShownGoalAchievement = true
        }

        view?.updateWaterProgress(updatedIntake)
        updateProgressView()
    }

    override fun setDailyGoal(goal: Int) {
        if (goal < 500 || goal > 5000) {
            view?.showError("Daily goal should be between 500ml and 5000ml")
            return
        }

        waterRepository.setDailyGoal(goal)
        val updatedIntake = waterRepository.getCurrentWaterIntake()
        view?.updateWaterProgress(updatedIntake)
        updateProgressView()
        view?.showToast("Daily goal updated to ${goal}ml")
    }

    override fun resetDailyProgress() {
        waterRepository.resetDailyProgress()
        hasShownGoalAchievement = false
        val updatedIntake = waterRepository.getCurrentWaterIntake()
        view?.updateWaterProgress(updatedIntake)
        updateProgressView()
        view?.showToast("Daily progress reset")
    }

    private fun updateProgressView() {
        val currentIntake = waterRepository.getCurrentWaterIntake()
        view?.showGoalProgress(
            currentIntake.getPercentage(),
            currentIntake.isGoalAchieved()
        )
    }

    override fun onHistoryClicked() {
        view?.navigateToHistory()
    }

    override fun onSettingsClicked() {
        view?.navigateToSettings()
    }

    override fun onLogoutClicked() {
        loginRepository.logout()
        view?.navigateToLogin()
    }

    override fun onDestroy() {
        scope.cancel()
        view = null
    }
}