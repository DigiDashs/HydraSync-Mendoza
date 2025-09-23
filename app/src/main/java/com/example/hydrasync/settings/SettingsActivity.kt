package com.example.hydrasync.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hydrasync.R
import com.example.hydrasync.home.HomeActivity
import com.example.hydrasync.login.LoginActivity
//import com.example.hydrasync.profile.ProfileActivity
import com.example.hydrasync.settings.data.SettingsData
import com.example.hydrasync.history.HistoryActivity
import com.example.hydrasync.profile.ProfileActivity


class SettingsActivity : AppCompatActivity(), SettingsContract.View {

    // UI Components
    private lateinit var profileItem: LinearLayout
    private lateinit var dailyGoalItem: LinearLayout
    private lateinit var inactivityAlertItem: LinearLayout
    private lateinit var quietHoursItem: LinearLayout
    private lateinit var btnLogout: Button

    // Text views for displaying current values
    private lateinit var tvDailyGoalValue: TextView
    private lateinit var tvInactivityAlertValue: TextView
    private lateinit var tvQuietHoursValue: TextView

    // Navigation tabs
    private lateinit var tabHistory: LinearLayout
    private lateinit var tabHome: LinearLayout
    private lateinit var tabSettings: LinearLayout

    // Presenter
    private lateinit var presenter: SettingsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        presenter = SettingsPresenter(this)
        setupClickListeners()

        presenter.loadSettingsData()
    }

    private fun initViews() {
        // Settings items
        profileItem = findViewById(R.id.profileItem)
        dailyGoalItem = findViewById(R.id.dailyGoalItem)
        inactivityAlertItem = findViewById(R.id.inactivityAlertItem)
        quietHoursItem = findViewById(R.id.quietHoursItem)
        btnLogout = findViewById(R.id.btnLogout)

        // Value text views (you'll need to add IDs to these in your XML)
        tvDailyGoalValue = dailyGoalItem.findViewById<TextView>(R.id.tvDailyGoalValue) // Add this ID to XML
        tvInactivityAlertValue = inactivityAlertItem.findViewById<TextView>(R.id.tvInactivityAlertValue) // Add this ID to XML
        tvQuietHoursValue = quietHoursItem.findViewById<TextView>(R.id.tvQuietHoursValue) // Add this ID to XML

        // Bottom navigation
        tabHistory = findViewById(R.id.tabHistory)
        tabHome = findViewById(R.id.tabHome)
        tabSettings = findViewById(R.id.tabSettings)
    }

    private fun setupClickListeners() {
        // Settings items click listeners
        profileItem.setOnClickListener { presenter.onProfileClicked() }
        dailyGoalItem.setOnClickListener { presenter.onDailyGoalClicked() }
        inactivityAlertItem.setOnClickListener { presenter.onInactivityAlertClicked() }
        quietHoursItem.setOnClickListener { presenter.onQuietHoursClicked() }
        btnLogout.setOnClickListener { presenter.onLogoutClicked() }

        // Bottom navigation click listeners
        tabHistory.setOnClickListener { presenter.onHistoryClicked() }
        tabHome.setOnClickListener { presenter.onHomeClicked() }
        tabSettings.setOnClickListener { /* Already on settings */ }
    }

    // Contract implementation
    override fun displaySettingsData(settingsData: SettingsData) {
        tvDailyGoalValue.text = settingsData.getDailyGoalText()
        tvInactivityAlertValue.text = settingsData.getInactivityAlertText()
        tvQuietHoursValue.text = settingsData.getQuietHoursText()
    }

    override fun updateDailyGoal(goalML: Int) {
        tvDailyGoalValue.text = "$goalML mL"
    }

    override fun updateInactivityAlert(minutes: Int) {
        tvInactivityAlertValue.text = "$minutes mins"
    }

    override fun updateQuietHours(startTime: String, endTime: String) {
        tvQuietHoursValue.text = "$startTime-$endTime"
    }

    override fun showDailyGoalDialog(currentGoal: Int) {
        val goals = arrayOf("1500 mL", "2000 mL", "2500 mL", "3000 mL", "Custom")
        val goalValues = intArrayOf(1500, 2000, 2500, 3000, -1)

        AlertDialog.Builder(this)
            .setTitle("Select Daily Goal")
            .setItems(goals) { _, which ->
                if (goalValues[which] == -1) {
                    // TODO: Show custom input dialog
                    showToast("Custom goal input coming soon!")
                } else {
                    presenter.updateDailyGoal(goalValues[which])
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showInactivityAlertDialog(currentMinutes: Int) {
        val intervals = arrayOf("30 minutes", "60 minutes", "90 minutes", "120 minutes")
        val intervalValues = intArrayOf(30, 60, 90, 120)

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
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    override fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    override fun navigateToHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}