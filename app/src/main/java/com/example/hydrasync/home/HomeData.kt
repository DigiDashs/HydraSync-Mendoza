package com.example.hydrasync.home

import kotlin.math.min

data class WaterIntake(
    val currentIntake: Int = 0,
    val dailyGoal: Int = 2000,
    val lastDrink: String = "0 ML",
    val timeAgo: String = "Never",
    val lastDrinkTime: Long = System.currentTimeMillis()
) {
    // ALWAYS cap percentage at 100% to prevent UI overflow
    fun getPercentage(): Int = min(100, ((currentIntake.toFloat() / dailyGoal) * 100).toInt())

    fun getProgressText(): String = "$currentIntake / ${dailyGoal}ml"

    // Check if daily goal is achieved
    fun isGoalAchieved(): Boolean = currentIntake >= dailyGoal

    // Get remaining amount to reach goal (never negative)
    fun getRemainingIntake(): Int = maxOf(0, dailyGoal - currentIntake)

    // Get amount exceeded if over goal
    fun getExceededAmount(): Int = maxOf(0, currentIntake - dailyGoal)

    // Get motivational status text
    fun getStatusText(): String {
        return when {
            currentIntake == 0 -> "Let's start hydrating!"
            isGoalAchieved() && getExceededAmount() > 0 -> "Goal Reached! +${getExceededAmount()} mL"
            isGoalAchieved() -> "Goal Reached! 🎉"
            getPercentage() >= 75 -> "Almost there! ${getRemainingIntake()} mL to go"
            getPercentage() >= 50 -> "Great progress! Keep it up"
            getPercentage() >= 25 -> "Good start! ${getRemainingIntake()} mL remaining"
            else -> "${getRemainingIntake()} mL to reach your goal"
        }
    }

    // Get formatted time ago
    fun getFormattedTimeAgo(): String {
        if (currentIntake == 0) return "No drinks yet today"

        val diffInMillis = System.currentTimeMillis() - lastDrinkTime
        val diffInMinutes = diffInMillis / (1000 * 60)

        return when {
            diffInMinutes < 1 -> "Just now"
            diffInMinutes < 60 -> "${diffInMinutes}m ago"
            diffInMinutes < 1440 -> "${diffInMinutes / 60}h ago"
            else -> "${diffInMinutes / 1440}d ago"
        }
    }

    // Get progress color based on achievement
    fun getProgressColor(): String {
        return when {
            isGoalAchieved() -> "#4CAF50" // Green - goal achieved
            getPercentage() >= 75 -> "#2196F3" // Blue - almost there
            getPercentage() >= 50 -> "#03A9F4" // Light blue - good progress
            getPercentage() >= 25 -> "#00BCD4" // Cyan - getting started
            else -> "#B0BEC5" // Gray - just started
        }
    }
}

data class HomeData(
    val user: com.example.hydrasync.login.User,
    val waterIntake: WaterIntake,
    val isConnected: Boolean = true,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val goalAchievedToday: Boolean = false
) {
    fun getConnectionStatusText(): String {
        return if (isConnected) "Connected" else "Disconnected"
    }

    fun getLastSyncText(): String {
        val diffInMinutes = (System.currentTimeMillis() - lastSyncTime) / (1000 * 60)
        return when {
            diffInMinutes < 1 -> "Synced just now"
            diffInMinutes < 60 -> "Synced ${diffInMinutes}m ago"
            else -> "Synced ${diffInMinutes / 60}h ago"
        }
    }
}