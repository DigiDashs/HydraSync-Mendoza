package com.example.hydrasync.home

data class WaterIntake(
    val currentIntake: Int = 0,
    val dailyGoal: Int = 2000,
    val lastDrink: String = "250 mL",
    val timeAgo: String = "1 day ago"
) {
    fun getPercentage(): Int = ((currentIntake.toFloat() / dailyGoal) * 100).toInt()
    fun getProgressText(): String = "$currentIntake / ${dailyGoal}ml"
}

data class HomeData(
    val user: com.example.hydrasync.login.User,
    val waterIntake: WaterIntake,
    val isConnected: Boolean = true
)