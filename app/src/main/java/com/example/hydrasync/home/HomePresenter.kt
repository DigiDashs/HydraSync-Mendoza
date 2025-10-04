package com.example.hydrasync.home

import com.example.hydrasync.data.WaterIntakeRepository
import com.example.hydrasync.login.LoginRepository
import com.example.hydrasync.settings.SettingsRepository
import kotlinx.coroutines.*

class HomePresenter(private var view: HomeContract.View?) : HomeContract.Presenter {

    private val loginRepository = LoginRepository.getInstance()
    private val waterRepository = WaterIntakeRepository.getInstance()
    private val settingsRepository = SettingsRepository()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var hasShownGoalAchievement = false

    override fun loadHomeData() {
        scope.launch {
            val currentUser = loginRepository.getCurrentUser()

            if (currentUser != null) {
                try {
                    // Load daily goal from Firebase
                    val dailyGoal = withContext(Dispatchers.IO) {
                        settingsRepository.getDailyGoal()
                    }

                    // Update repository with the goal
                    waterRepository.setDailyGoal(dailyGoal)

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
                    // Handle error - use default goal
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
                view?.navigateToLogin()
            }
        }
    }

    override fun onAddIntakeClicked() {
        view?.showAddIntakeDialog()
    }

    override fun addWaterIntake(amount: Int) {
        val currentIntake = waterRepository.getCurrentWaterIntake()
        val wasGoalAchieved = currentIntake.isGoalAchieved()

        // Add to repository - continue recording even if goal is achieved
        waterRepository.addDrinkEntry(amount)

        // Get updated intake status
        val updatedIntake = waterRepository.getCurrentWaterIntake()

        // Check if goal was JUST achieved (one-time trigger)
        if (!wasGoalAchieved && updatedIntake.isGoalAchieved() && !hasShownGoalAchievement) {
            view?.showGoalAchieved()
            hasShownGoalAchievement = true
            // TODO: Disable reminders for the day
        }

        view?.updateWaterProgress(updatedIntake)
        updateProgressView()

        view?.showToast("Added ${amount}ml - ${updatedIntake.getStatusText()}")
    }

    override fun setDailyGoal(goal: Int) {
        // This method is now handled by Settings
        // Removed to prevent conflicts
        view?.showToast("Please use Settings to change your daily goal")
    }

    override fun resetDailyProgress() {
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