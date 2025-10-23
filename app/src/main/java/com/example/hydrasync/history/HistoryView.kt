package com.example.hydrasync.history

interface HistoryView {
    fun showHistoryByDate(historyByDate: Map<String, List<DrinkEntry>>)
    fun showEmptyState(show: Boolean)
    fun showLoadingState(show: Boolean)
    fun showError(message: String)

    // Interactive methods
    fun showToast(message: String)
    fun showEditDialog(entry: DrinkEntry)
    fun showDeleteConfirmation(entry: DrinkEntry)
}

