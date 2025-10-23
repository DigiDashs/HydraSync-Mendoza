package com.example.hydrasync.history

import android.util.Log
import com.example.hydrasync.data.WaterIntakeRepository
import com.example.hydrasync.data.WaterLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryPresenter(private val view: HistoryView) {

    private val repository = WaterIntakeRepository.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadDrinkHistory() {
        view.showLoadingState(true)

        scope.launch {
            try {
                val allWaterLogs = withContext(Dispatchers.IO) {
                    repository.getAllWaterLogs()
                }

                if (allWaterLogs.isEmpty()) {
                    view.showEmptyState(true)
                    view.showLoadingState(false)
                    return@launch
                }

                // Group water logs by date
                val historyByDate = mutableMapOf<String, MutableList<DrinkEntry>>()

                allWaterLogs.forEach { waterLog ->
                    val drinkEntry = waterLog.toDrinkEntry()
                    val date = drinkEntry.date

                    if (!historyByDate.containsKey(date)) {
                        historyByDate[date] = mutableListOf()
                    }
                    historyByDate[date]?.add(drinkEntry)
                }

                // Sort dates in descending order (newest first)
                val sortedHistory = historyByDate.toSortedMap(compareByDescending { it })

                view.showHistoryByDate(sortedHistory)
                view.showEmptyState(false)

            } catch (e: Exception) {
                Log.e("HistoryPresenter", "Error loading history: ${e.message}", e)
                view.showError("Failed to load history: ${e.message}")
            } finally {
                view.showLoadingState(false)
            }
        }
    }

    // Delete entry from Firebase
    fun deleteEntry(entry: DrinkEntry) {
        scope.launch {
            try {
                // We need to find the corresponding WaterLog
                val allWaterLogs = withContext(Dispatchers.IO) {
                    repository.getAllWaterLogs()
                }

                val waterLogToDelete = allWaterLogs.find { waterLog ->
                    val drinkEntry = waterLog.toDrinkEntry()
                    drinkEntry.time == entry.time &&
                            drinkEntry.amount == entry.amount
                }

                if (waterLogToDelete != null) {
                    val success = withContext(Dispatchers.IO) {
                        repository.deleteWaterLog(waterLogToDelete.id)
                    }

                    if (success) {
                        view.showToast("Entry deleted")
                        loadDrinkHistory() // Refresh the view
                    } else {
                        view.showError("Failed to delete entry")
                    }
                } else {
                    view.showError("Entry not found in database")
                }
            } catch (e: Exception) {
                Log.e("HistoryPresenter", "Error deleting entry: ${e.message}", e)
                view.showError("Failed to delete entry: ${e.message}")
            }
        }
    }

    // Edit entry in Firebase
    fun editEntry(oldEntry: DrinkEntry, newAmount: Int) {
        scope.launch {
            try {
                // Find the corresponding WaterLog
                val allWaterLogs = withContext(Dispatchers.IO) {
                    repository.getAllWaterLogs()
                }

                val waterLogToUpdate = allWaterLogs.find { waterLog ->
                    val drinkEntry = waterLog.toDrinkEntry()
                    drinkEntry.time == oldEntry.time &&
                            drinkEntry.amount == oldEntry.amount
                }

                if (waterLogToUpdate != null) {
                    val success = withContext(Dispatchers.IO) {
                        repository.updateWaterLog(waterLogToUpdate.id, newAmount)
                    }

                    if (success) {
                        view.showToast("Entry updated")
                        loadDrinkHistory() // Refresh the view
                    } else {
                        view.showError("Failed to update entry")
                    }
                } else {
                    view.showError("Entry not found in database")
                }
            } catch (e: Exception) {
                Log.e("HistoryPresenter", "Error updating entry: ${e.message}", e)
                view.showError("Failed to update entry: ${e.message}")
            }
        }
    }

    fun formatTotal(totalMl: Int): String {
        return when {
            totalMl >= 1000 -> String.format("%.1f L", totalMl / 1000.0)
            else -> "$totalMl mL"
        }
    }

    fun onDestroy() {
        scope.cancel()
    }
}