package com.example.hydrasync.history

import com.example.hydrasync.history.DrinkEntry

interface   HistoryView {
    fun showHistory(historyItems: List<DrinkEntry>)
    fun setTodayTotal(total: String)
    fun setYesterdayTotal(total: String)
}
