package com.example.hydrasync.history

import com.example.hydrasync.data.WaterIntakeRepository

class HistoryPresenter(private val view: HistoryView) {

    private val repository = WaterIntakeRepository.getInstance()

    fun loadDrinkHistory() {
        view.showLoadingState(true)

        try {
            // Add sample data for testing
            repository.addSampleData()

            val allEntries = repository.getAllEntries()

            if (allEntries.isEmpty()) {
                view.showEmptyState(true)
                view.showLoadingState(false)
                return
            }

            val todayEntries = repository.getTodayEntries()
            val yesterdayEntries = repository.getYesterdayEntries()

            val todayTotal = repository.getTodayTotalIntake()
            val yesterdayTotal = repository.getTotalIntakeForDate("Yesterday")

            view.showTodayHistory(todayEntries)
            view.showYesterdayHistory(yesterdayEntries)
            view.setTodayTotal(todayTotal)
            view.setYesterdayTotal(yesterdayTotal)
            view.showEmptyState(false)

        } catch (e: Exception) {
            view.showError("Failed to load history: ${e.message}")
        } finally {
            view.showLoadingState(false)
        }
    }

    // Interactive features
    fun deleteEntry(entry: DrinkEntry) {
        if (repository.deleteEntry(entry)) {
            view.showToast("Entry deleted")
            loadDrinkHistory() // Refresh the view
        } else {
            view.showError("Failed to delete entry")
        }
    }

    fun editEntry(oldEntry: DrinkEntry, newAmount: Int) {
        val updatedEntry = repository.updateEntry(oldEntry, newAmount)
        if (updatedEntry != null) {
            view.showToast("Entry updated")
            loadDrinkHistory() // Refresh the view
        } else {
            view.showError("Failed to update entry")
        }
    }

    fun formatTotal(totalMl: Int): String {
        return when {
            totalMl >= 1000 -> String.format("%.1f L", totalMl / 1000.0)
            else -> "$totalMl mL"
        }
    }
}