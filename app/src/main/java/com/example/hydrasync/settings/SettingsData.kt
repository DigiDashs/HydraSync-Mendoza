package com.example.hydrasync.settings.data

data class SettingsData(
    val dailyGoalML: Int,
    val inactivityAlertMinutes: Int,
    val quietHoursStart: String,
    val quietHoursEnd: String,
    val userName: String,
    val userEmail: String
) {
    fun getDailyGoalText(): String = "$dailyGoalML mL"

    fun getInactivityAlertText(): String = "$inactivityAlertMinutes mins"

    fun getQuietHoursText(): String = "$quietHoursStart-$quietHoursEnd"

    companion object {
        fun getDefault(): SettingsData {
            return SettingsData(
                dailyGoalML = 2000,
                inactivityAlertMinutes = 60,
                quietHoursStart = "10PM",
                quietHoursEnd = "7AM",
                userName = "User",
                userEmail = "user@example.com"
            )
        }
    }
}

data class TimeRange(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
) {
    fun getStartTimeFormatted(): String {
        val period = if (startHour >= 12) "PM" else "AM"
        val hour = if (startHour == 0) 12 else if (startHour > 12) startHour - 12 else startHour
        return "${hour}${period}"
    }

    fun getEndTimeFormatted(): String {
        val period = if (endHour >= 12) "PM" else "AM"
        val hour = if (endHour == 0) 12 else if (endHour > 12) endHour - 12 else endHour
        return "${hour}${period}"
    }
}