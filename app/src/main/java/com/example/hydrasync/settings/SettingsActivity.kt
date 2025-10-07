package com.example.hydrasync.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hydrasync.R
import com.example.hydrasync.home.HomeActivity
import com.example.hydrasync.login.LoginActivity
import com.example.hydrasync.settings.data.SettingsData
import com.example.hydrasync.history.HistoryActivity
import com.example.hydrasync.profile.ProfileActivity

class SettingsActivity : AppCompatActivity(), SettingsContract.View {

    private lateinit var profileItem: LinearLayout
    private lateinit var dailyGoalItem: LinearLayout
    private lateinit var inactivityAlertItem: LinearLayout
    private lateinit var quietHoursItem: LinearLayout
    private lateinit var btnLogout: Button

    private lateinit var tvDailyGoalValue: TextView
    private lateinit var tvInactivityAlertValue: TextView
    private lateinit var tvQuietHoursValue: TextView

    private lateinit var tabHistory: LinearLayout
    private lateinit var tabHome: LinearLayout
    private lateinit var tabSettings: LinearLayout

    private lateinit var presenter: SettingsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        presenter = SettingsPresenter(this)
        setupClickListeners()

        createNotificationChannel()
        presenter.loadSettingsData()
    }

    private fun initViews() {
        profileItem = findViewById(R.id.profileItem)
        dailyGoalItem = findViewById(R.id.dailyGoalItem)
        inactivityAlertItem = findViewById(R.id.inactivityAlertItem)
        quietHoursItem = findViewById(R.id.quietHoursItem)
        btnLogout = findViewById(R.id.btnLogout)

        tvDailyGoalValue = dailyGoalItem.findViewById(R.id.tvDailyGoalValue)
        tvInactivityAlertValue = inactivityAlertItem.findViewById(R.id.tvInactivityAlertValue)
        tvQuietHoursValue = quietHoursItem.findViewById(R.id.tvQuietHoursValue)

        tabHistory = findViewById(R.id.tabHistory)
        tabHome = findViewById(R.id.tabHome)
        tabSettings = findViewById(R.id.tabSettings)
    }

    private fun setupClickListeners() {
        profileItem.setOnClickListener { presenter.onProfileClicked() }
        dailyGoalItem.setOnClickListener { presenter.onDailyGoalClicked() }
        inactivityAlertItem.setOnClickListener { presenter.onInactivityAlertClicked() }
        quietHoursItem.setOnClickListener { presenter.onQuietHoursClicked() }
        btnLogout.setOnClickListener { presenter.onLogoutClicked() }

        tabHistory.setOnClickListener { presenter.onHistoryClicked() }
        tabHome.setOnClickListener { presenter.onHomeClicked() }
        tabSettings.setOnClickListener { /* Already on settings */ }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "inactivity_channel",
                "Inactivity Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminders when you are inactive" }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun displaySettingsData(settingsData: SettingsData) {
        tvDailyGoalValue.text = settingsData.getDailyGoalText()
        tvInactivityAlertValue.text = settingsData.getInactivityAlertText()
        tvQuietHoursValue.text = settingsData.getQuietHoursText()
    }

    override fun updateDailyGoal(goalML: Int) {
        tvDailyGoalValue.text = "$goalML mL"
    }

    override fun updateInactivityAlert(minutes: Int) {
        tvInactivityAlertValue.text = if (minutes == 0) "No Alerts" else "$minutes mins"
    }

    override fun updateQuietHours(startTime: String, endTime: String) {
        tvQuietHoursValue.text = "$startTime-$endTime"
    }

    override fun showDailyGoalDialog(currentGoal: Int) {
        val goals = arrayOf("1500 mL", "2000 mL", "2500 mL", "3000 mL", "Custom")
        val goalValues = intArrayOf(1500, 2000, 2500, 3000, -1)

        AlertDialog.Builder(this)
            .setTitle("Select Daily Goal\nCurrent: $currentGoal mL ")
            .setItems(goals) { _, which ->
                if (which == goals.size - 1) {
                    showCustomGoalDialog()
                } else {
                    presenter.updateDailyGoal(goalValues[which])
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomGoalDialog() {
        val input = EditText(this).apply {
            hint = "Enter goal (500-5000 mL)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle("Custom Daily Goal")
            .setMessage("Recommended: 2000 mL\nMin: 500 mL | Max: 5000 mL")
            .setView(input)
            .setPositiveButton("Set") { _, _ ->
                val customGoal = input.text.toString().toIntOrNull()
                if (customGoal != null) {
                    if (customGoal in 500..5000) {
                        presenter.updateDailyGoal(customGoal)
                    } else {
                        showToast("Please enter a value between 500 and 5000 mL")
                    }
                } else {
                    showToast("Please enter a valid number")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showInactivityAlertDialog(currentMinutes: Int) {
        val intervals = arrayOf("No Alerts", "30 minutes", "60 minutes", "90 minutes", "120 minutes")
        val intervalValues = intArrayOf(0, 30, 60, 90, 120)

        AlertDialog.Builder(this)
            .setTitle("Select Inactivity Alert")
            .setItems(intervals) { _, which ->
                presenter.updateInactivityAlert(intervalValues[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showQuietHoursDialog(startTime: String, endTime: String) {
        val timeRanges = arrayOf("10PM-7AM", "11PM-6AM", "9PM-8AM", "Custom")

        AlertDialog.Builder(this)
            .setTitle("Select Quiet Hours")
            .setItems(timeRanges) { _, which ->
                when (which) {
                    0 -> presenter.updateQuietHours("10PM", "7AM")
                    1 -> presenter.updateQuietHours("11PM", "6AM")
                    2 -> presenter.updateQuietHours("9PM", "8AM")
                    3 -> showToast("Custom time picker coming soon!")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> presenter.onLogoutConfirmed() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    override fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish() // Finish settings so Home refreshes
    }

    override fun navigateToHistory() {
        startActivity(Intent(this, HistoryActivity::class.java))
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
