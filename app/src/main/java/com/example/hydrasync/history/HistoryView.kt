package com.example.hydrasync.history

interface HistoryView {
    fun showTodayHistory(entries: List<DrinkEntry>)
    fun showYesterdayHistory(entries: List<DrinkEntry>)
    fun setTodayTotal(totalMl: Int)
    fun setYesterdayTotal(totalMl: Int)
    fun showEmptyState(show: Boolean)
    fun showLoadingState(show: Boolean)
    fun showError(message: String)

    // Add these new methods to make it interactive
    fun showToast(message: String)
    fun showEditDialog(entry: DrinkEntry)
    fun showDeleteConfirmation(entry: DrinkEntry)
}

