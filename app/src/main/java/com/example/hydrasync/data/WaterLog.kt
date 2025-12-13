package com.example.hydrasync.data

import com.example.hydrasync.history.DrinkEntry
import com.google.firebase.database.Exclude
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WaterLog(
    val id: String = "",
    val amount: Int = 0,
    val timestamp: Long = 0,
    val date: String = ""
) {
    @Exclude
    fun toDrinkEntry(): DrinkEntry {
        return DrinkEntry(
            time = formatTime(timestamp),
            amount = "$amount ML",
            date = formatDisplayDate(date)
        )
    }

    companion object {
        private fun formatTime(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            return format.format(date)
        }

        private fun formatDisplayDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString) ?: Date() //
                outputFormat.format(date)
            } catch (e: Exception) {
                // Fallback to today's date
                val todayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                todayFormat.format(Date())
            }
        }

        fun getCurrentFormattedDate(): String {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return dateFormat.format(Date())
        }

        fun getCurrentTimestampDate(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }
}