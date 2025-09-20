package com.example.hydrasync.home

import com.example.hydrasync.login.LoginRepository
import kotlinx.coroutines.*

class HomePresenter(private var view: HomeContract.View?) : HomeContract.Presenter {

    private val repository = LoginRepository.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentWaterIntake = WaterIntake()

    override fun loadHomeData() {
        val currentUser = repository.getCurrentUser()

        if (currentUser != null) {
            val homeData = HomeData(
                user = currentUser,
                waterIntake = currentWaterIntake,
                isConnected = true
            )
            view?.displayHomeData(homeData)
        } else {
            view?.navigateToLogin()
        }
    }

    override fun onAddIntakeClicked() {
        view?.showAddIntakeDialog()
    }

    override fun addWaterIntake(amount: Int) {
        currentWaterIntake = currentWaterIntake.copy(
            currentIntake = currentWaterIntake.currentIntake + amount,
            lastDrink = "${amount} mL",
            timeAgo = "Just now"
        )
        view?.updateWaterProgress(currentWaterIntake)
    }

    override fun onHistoryClicked() {
        view?.navigateToHistory()
    }

    override fun onSettingsClicked() {
        view?.navigateToSettings()
    }

    override fun onLogoutClicked() {
        repository.logout()
        view?.navigateToLogin()
    }

    override fun onDestroy() {
        scope.cancel()
        view = null
    }
}