package com.example.hydrasync.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hydrasync.R
import com.example.hydrasync.history.HistoryActivity
import com.example.hydrasync.login.LoginActivity
import com.example.hydrasync.settings.SettingsActivity
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var tvUserName: TextView
    private lateinit var tvPercentage: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvLastDrink: TextView
    private lateinit var tvTimeAgo: TextView
    private lateinit var tvConnectionStatus: TextView
    private lateinit var btnAddIntake: MaterialButton
    private lateinit var tabHistory: LinearLayout
    private lateinit var tabHome: LinearLayout
    private lateinit var tabSettings: LinearLayout
    private lateinit var ivProfile: ImageView
    private lateinit var progressBar: ProgressBar

    private lateinit var presenter: HomePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
        presenter = HomePresenter(this)
        setupClickListeners()

        presenter.loadHomeData()
    }

    override fun onResume() {
        super.onResume()
        presenter.loadHomeData()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvPercentage = findViewById(R.id.tvPercentage)
        tvProgress = findViewById(R.id.tvProgress)
        tvLastDrink = findViewById(R.id.tvLastDrink)
        tvTimeAgo = findViewById(R.id.tvTimeAgo)
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus)
        btnAddIntake = findViewById(R.id.btnAddIntake)
        tabHistory = findViewById(R.id.tabHistory)
        tabHome = findViewById(R.id.tabHome)
        tabSettings = findViewById(R.id.tabSettings)
        ivProfile = findViewById(R.id.ivProfile)
        progressBar = findViewById(R.id.circularProgress)

    }

    private fun setupClickListeners() {
        btnAddIntake.setOnClickListener { presenter.onAddIntakeClicked() }
        tabHistory.setOnClickListener { presenter.onHistoryClicked() }
        tabSettings.setOnClickListener { presenter.onSettingsClicked() }
        ivProfile.setOnClickListener { presenter.onLogoutClicked() }
    }

    override fun displayHomeData(homeData: HomeData) {
        tvUserName.text = "Welcome, ${homeData.user.getFullName()}!"
        updateWaterProgress(homeData.waterIntake)

        tvConnectionStatus.text = homeData.getConnectionStatusText()
        tvConnectionStatus.setTextColor(
            if (homeData.isConnected)
                ContextCompat.getColor(this, R.color.hydra_green)
            else
                ContextCompat.getColor(this, R.color.gray)
        )
    }

    override fun updateWaterProgress(intake: WaterIntake) {
        val percentage = intake.getPercentage()
        tvPercentage.text = "$percentage%"
        tvProgress.text = intake.getProgressText()
        tvLastDrink.text = "Last Drink: ${intake.lastDrink}"
        tvTimeAgo.text = intake.getFormattedTimeAgo()

        progressBar.max = 100
        progressBar.progress = percentage

        if (intake.isGoalAchieved()) {
            if (intake.getExceededAmount() > 0) {
                btnAddIntake.text = "Goal Reached! +${intake.getExceededAmount()}mL extra"
            } else {
                btnAddIntake.text = "Goal Achieved! + Add More"
            }
        } else {
            val remaining = intake.getRemainingIntake()
            btnAddIntake.text = if (remaining > 0) {
                "+ Add Intake (${remaining}ml to go)"
            } else {
                "+ Add Intake"
            }
        }

    }

    override fun showGoalProgress(percentage: Int, isGoalAchieved: Boolean) {
        val color = if (isGoalAchieved) {
            ContextCompat.getColor(this, R.color.hydra_green) // Green
        } else {
            ContextCompat.getColor(this, R.color.hydra_blue) // Blue
        }

        tvPercentage.setTextColor(color)
        tvProgress.setTextColor(color)
    }

    override fun showAddIntakeDialog() {
        val amounts = arrayOf("250ml", "500ml", "750ml", "1000ml", "Custom")
        val values = intArrayOf(250, 500, 750, 1000, 0)

        AlertDialog.Builder(this)
            .setTitle("Add Water Intake")
            .setItems(amounts) { _, which ->
                if (which == amounts.size - 1) {
                    showCustomAmountDialog()
                } else {
                    presenter.addWaterIntake(values[which])
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomAmountDialog() {
        val input = EditText(this).apply {
            hint = "Enter amount in ml"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle("Custom Amount")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toIntOrNull()
                if (amount != null && amount > 0 && amount <= 5000) { // Increased max to 5000ml
                    presenter.addWaterIntake(amount)
                } else {
                    showError("Please enter a valid amount (1-5000ml)")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showGoalAchieved() {
        AlertDialog.Builder(this)
            .setTitle("Congratulations!")
            .setMessage("You've achieved your daily water intake goal! Great job staying hydrated!")
            .setPositiveButton("Awesome!") { _, _ -> }
            .show()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun navigateToHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    override fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}