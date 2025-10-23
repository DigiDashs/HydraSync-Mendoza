package com.example.hydrasync.data

import android.util.Log
import com.example.hydrasync.history.DrinkEntry
import com.example.hydrasync.home.WaterIntake
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class WaterIntakeRepository private constructor() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://hydrasync-14b0a-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val userRepository = UserRepository.getInstance()

    companion object {
        @Volatile
        private var INSTANCE: WaterIntakeRepository? = null

        fun getInstance(): WaterIntakeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WaterIntakeRepository().also { INSTANCE = it }
            }
        }
    }

    // Get reference to current user's water logs
    private fun getWaterLogsReference(): DatabaseReference? {
        val userRef = userRepository.getUserReference()
        return userRef?.child("waterLogs")
    }

    // Add new drink entry to Firebase
    suspend fun addDrinkEntry(amountMl: Int): Boolean {
        return try {
            val waterLogsRef = getWaterLogsReference() ?: return false

            val waterLog = WaterLog(
                id = waterLogsRef.push().key ?: UUID.randomUUID().toString(),
                amount = amountMl,
                timestamp = System.currentTimeMillis(),
                date = WaterLog.getCurrentTimestampDate()
            )

            waterLogsRef.child(waterLog.id).setValue(waterLog).await()
            true
        } catch (e: Exception) {
            Log.e("WaterIntakeRepository", "Error adding drink entry: ${e.message}", e)
            false
        }
    }

    // Get all water logs from Firebase
    suspend fun getAllWaterLogs(): List<WaterLog> {
        return try {
            val waterLogsRef = getWaterLogsReference() ?: return emptyList()
            val snapshot = waterLogsRef.get().await()

            val waterLogs = mutableListOf<WaterLog>()
            for (child in snapshot.children) {
                val waterLog = child.getValue(WaterLog::class.java)
                waterLog?.let {
                    waterLogs.add(it.copy(id = child.key ?: ""))
                }
            }

            // Sort by timestamp descending (newest first)
            waterLogs.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e("WaterIntakeRepository", "Error getting water logs: ${e.message}", e)
            emptyList()
        }
    }

    // Get today's water logs
    suspend fun getTodayWaterLogs(): List<WaterLog> {
        val allLogs = getAllWaterLogs()
        val todayDate = WaterLog.getCurrentFormattedDate()
        return allLogs.filter { it.toDrinkEntry().date == todayDate }
    }

    // Calculate total intake for today
    suspend fun getTodayTotalIntake(): Int {
        val todayLogs = getTodayWaterLogs()
        return todayLogs.sumOf { it.amount }
    }

    // Delete water log entry
    suspend fun deleteWaterLog(waterLogId: String): Boolean {
        return try {
            val waterLogsRef = getWaterLogsReference() ?: return false
            waterLogsRef.child(waterLogId).removeValue().await()
            true
        } catch (e: Exception) {
            Log.e("WaterIntakeRepository", "Error deleting water log: ${e.message}", e)
            false
        }
    }

    // Update water log entry
    suspend fun updateWaterLog(waterLogId: String, newAmount: Int): Boolean {
        return try {
            val waterLogsRef = getWaterLogsReference() ?: return false
            val updates = mapOf(
                "amount" to newAmount,
                "timestamp" to System.currentTimeMillis(),
                "date" to WaterLog.getCurrentTimestampDate()
            )
            waterLogsRef.child(waterLogId).updateChildren(updates).await()
            true
        } catch (e: Exception) {
            Log.e("WaterIntakeRepository", "Error updating water log: ${e.message}", e)
            false
        }
    }

    // Get water log by ID
    suspend fun getWaterLogById(waterLogId: String): WaterLog? {
        return try {
            val waterLogsRef = getWaterLogsReference() ?: return null
            val snapshot = waterLogsRef.child(waterLogId).get().await()
            snapshot.getValue(WaterLog::class.java)?.copy(id = waterLogId)
        } catch (e: Exception) {
            null
        }
    }
}