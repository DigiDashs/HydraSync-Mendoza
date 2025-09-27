package com.example.hydrasync.data

import com.example.hydrasync.history.DrinkEntry
import com.example.hydrasync.home.WaterIntake
import java.text.SimpleDateFormat
import java.util.*

class WaterIntakeRepository private constructor() {

    // In-memory storage (replace with Room database later)
    private val drinkEntries = mutableListOf<DrinkEntry>()
    private var dailyGoal = 2000

    companion object {
        @Volatile
        private var INSTANCE: WaterIntakeRepository? = null

        fun getInstance(): WaterIntakeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WaterIntakeRepository().also { INSTANCE = it }
            }
        }

        private fun getTodayDate(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        private fun getYesterdayDate(): String {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        }

        private fun getCurrentTime(): String {
            return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        }
    }

    // Add new drink entry (called from Home when user adds intake)
    fun addDrinkEntry(amountMl: Int): DrinkEntry {
        val entry = DrinkEntry(
            time = getCurrentTime(),
            amount = "$amountMl ML",
            date = "Today"
        )
        drinkEntries.add(entry)
        return entry
    }

    // Get all entries for History screen
    fun getAllEntries(): List<DrinkEntry> {
        return drinkEntries.toList()
    }

    // Get entries for specific date
    fun getEntriesForDate(dateFilter: String): List<DrinkEntry> {
        return drinkEntries.filter { it.date == dateFilter }
    }

    // Get today's entries for Home screen
    fun getTodayEntries(): List<DrinkEntry> {
        return getEntriesForDate("Today")
    }

    // Get yesterday's entries
    fun getYesterdayEntries(): List<DrinkEntry> {
        return getEntriesForDate("Yesterday")
    }

    // Calculate total intake for date
    fun getTotalIntakeForDate(dateFilter: String): Int {
        return getEntriesForDate(dateFilter).sumOf {
            it.amount.replace(" ML", "").toIntOrNull() ?: 0
        }
    }

    // Get today's total for Home screen
    fun getTodayTotalIntake(): Int {
        return getTotalIntakeForDate("Today")
    }

    // Get current water intake status for Home
    fun getCurrentWaterIntake(): WaterIntake {
        val todayEntries = getTodayEntries()
        val totalIntake = todayEntries.sumOf {
            it.amount.replace(" ML", "").toIntOrNull() ?: 0
        }
        val lastEntry = todayEntries.lastOrNull()

        return WaterIntake(
            currentIntake = totalIntake,
            dailyGoal = dailyGoal,
            lastDrink = lastEntry?.amount ?: "0 ML",
            timeAgo = if (lastEntry != null) "Just now" else "Never"
        )
    }

    // Delete entry (for interactive features)
    fun deleteEntry(entry: DrinkEntry): Boolean {
        return drinkEntries.remove(entry)
    }

    // Update entry (for editing features)
    fun updateEntry(oldEntry: DrinkEntry, newAmountMl: Int): DrinkEntry? {
        val index = drinkEntries.indexOf(oldEntry)
        if (index != -1) {
            val updatedEntry = DrinkEntry(
                time = oldEntry.time,
                amount = "$newAmountMl ML",
                date = oldEntry.date
            )
            drinkEntries[index] = updatedEntry
            return updatedEntry
        }
        return null
    }

    // Set daily goal
    fun setDailyGoal(goal: Int) {
        dailyGoal = goal
    }

    fun getDailyGoal(): Int = dailyGoal

    // Reset daily progress (for new day)
    fun resetDailyProgress() {
        drinkEntries.removeAll { it.date == "Today" }
    }

    // Add some sample data for testing
    fun addSampleData() {
        if (drinkEntries.isEmpty()) {
            // Add sample entries
            drinkEntries.addAll(listOf(
                DrinkEntry("09:45 PM", "300 ML", "Yesterday"),
                DrinkEntry("03:20 PM", "500 ML", "Yesterday"),
                DrinkEntry("10:30 AM", "350 ML", "Yesterday")
            ))
        }
    }
}