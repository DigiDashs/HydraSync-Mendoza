package com.example.hydrasync.home

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

data class WaterIntake(
    val currentIntake: Int = 0,
    val dailyGoal: Int = 2000,
    val lastDrink: String = "250 mL",
    val timeAgo: String = "1 day ago",
    val lastDrinkTime: Long = System.currentTimeMillis()
) {
    // Fixed: Cap percentage at 100%
    fun getPercentage(): Int = min(100, ((currentIntake.toFloat() / dailyGoal) * 100).toInt())

    fun getProgressText(): String = "$currentIntake / ${dailyGoal}ml"

    // Check if daily goal is achieved
    fun isGoalAchieved(): Boolean = currentIntake >= dailyGoal

    // Get remaining amount to reach goal
    fun getRemainingIntake(): Int = maxOf(0, dailyGoal - currentIntake)

    // Get formatted time ago
    fun getFormattedTimeAgo(): String {
        val diffInMillis = System.currentTimeMillis() - lastDrinkTime
        val diffInMinutes = diffInMillis / (1000 * 60)

        return when {
            diffInMinutes < 1 -> "Just now"
            diffInMinutes < 60 -> "${diffInMinutes}m ago"
            diffInMinutes < 1440 -> "${diffInMinutes / 60}h ago"
            else -> "${diffInMinutes / 1440}d ago"
        }
    }

    // Get progress color based on percentage
    fun getProgressColor(): String {
        return when (getPercentage()) {
            in 0..25 -> "#FF6B6B" // Red - needs more water
            in 26..50 -> "#FFD93D" // Yellow - getting there
            in 51..75 -> "#6BCF7F" // Light green - good progress
            else -> "#4ECDC4" // Teal - excellent/goal achieved
        }
    }
}

data class HomeData(
    val user: com.example.hydrasync.login.User,
    val waterIntake: WaterIntake,
    val isConnected: Boolean = true,
    val lastSyncTime: Long = System.currentTimeMillis()
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