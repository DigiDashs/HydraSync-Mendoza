package com.example.hydrasync.data

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class WaterIntakeRepository private constructor() {

    private val userRepository = UserRepository.getInstance()

    // Store active listeners
    private var waterLogsListener: ValueEventListener? = null
    private var childEventListener: ChildEventListener? = null

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

    fun observeTodayTotalIntake(): Flow<Int> = callbackFlow {
        val waterLogsRef = getWaterLogsReference() ?: run {
            trySend(0)
            awaitClose()
            return@callbackFlow
        }

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val todayDate = WaterLog.getCurrentFormattedDate()
                var total = 0

                for (child in snapshot.children) {
                    val waterLog = child.getValue(WaterLog::class.java)
                    waterLog?.let {
                        if (it.toDrinkEntry().date == todayDate) {
                            total += it.amount
                        }
                    }
                }

                trySend(total).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WaterIntakeRepository", "Total intake listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        waterLogsRef.addValueEventListener(valueEventListener)
        waterLogsListener = valueEventListener

        awaitClose {
            waterLogsListener?.let { waterLogsRef.removeEventListener(it) }
        }
    }

    fun observeNewWaterLogs(): Flow<WaterLog> = callbackFlow {
        val waterLogsRef = getWaterLogsReference() ?: run {
            awaitClose()
            return@callbackFlow
        }

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val waterLog = snapshot.getValue(WaterLog::class.java)
                waterLog?.let {
                    Log.d("WaterIntakeRepository", "🆕 New water log detected: ${it.amount}ml")
                    trySend(it.copy(id = snapshot.key ?: "")).isSuccess
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WaterIntakeRepository", "New logs listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        waterLogsRef.addChildEventListener(childEventListener)
        this@WaterIntakeRepository.childEventListener = childEventListener

        awaitClose {
            childEventListener?.let { waterLogsRef.removeEventListener(it) }
        }
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

    // Get all water logs from Firebase (one-time)
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

    // Get today's water logs (one-time)
    suspend fun getTodayWaterLogs(): List<WaterLog> {
        val allLogs = getAllWaterLogs()
        val todayDate = WaterLog.getCurrentFormattedDate()
        return allLogs.filter { it.toDrinkEntry().date == todayDate }
    }

    // Calculate total intake for today (one-time)
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

    // Clean up listeners
    fun cleanup() {
        waterLogsListener?.let {
            getWaterLogsReference()?.removeEventListener(it)
            waterLogsListener = null
        }
        childEventListener?.let {
            getWaterLogsReference()?.removeEventListener(it)
            childEventListener = null
        }
    }
}