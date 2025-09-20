package com.example.hydrasync.history

class HistoryPresenter(private val view: HistoryView) {

    fun loadDrinkHistory() {
        // This is a simulated data fetch. In a real app, this would be from a database or API.
        val historyData = listOf(
            DrinkEntry("02:30 PM", "500 ML", "Today"),
            DrinkEntry("08:30 PM", "350 ML", "Yesterday"),
            DrinkEntry("12:00 PM", "200 ML", "Yesterday"),
            DrinkEntry("10:30 AM", "300 ML", "Yesterday"),
            DrinkEntry("08:00 AM", "450 ML", "Yesterday")
        )

        // Separate data into lists for "Today" and "Yesterday"
        val todayList = historyData.filter { it.date == "Today" }
        val yesterdayList = historyData.filter { it.date == "Yesterday" }

        // Calculate totals. Note: A more robust app would parse this with better error handling.
        val todayTotal = todayList.sumOf { it.amount.replace(" ML", "").toInt() } / 1000.0
        val yesterdayTotal = yesterdayList.sumOf { it.amount.replace(" ML", "").toInt() } / 1000.0

        // Call the view's methods to update the UI with the processed data
        view.setTodayTotal(String.format("%.1f", todayTotal))
        view.setYesterdayTotal(String.format("%.1f", yesterdayTotal))
        view.showHistory(historyData)
    }
}