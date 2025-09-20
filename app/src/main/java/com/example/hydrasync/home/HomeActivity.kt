package com.example.hydrasync.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
        tvUserName.text = homeData.user.getFullName()
        updateWaterProgress(homeData.waterIntake)

        tvConnectionStatus.text = if (homeData.isConnected) "Connected" else "Disconnected"
        tvConnectionStatus.setTextColor(
            if (homeData.isConnected)
                resources.getColor(R.color.hydra_green, null)
            else
                resources.getColor(R.color.gray, null)
        )
    }

    override fun updateWaterProgress(intake: WaterIntake) {
        tvPercentage.text = "${intake.getPercentage()}%"
        tvProgress.text = intake.getProgressText()
        tvLastDrink.text = "Last Drink: ${intake.lastDrink}"
        tvTimeAgo.text = intake.timeAgo
        progressBar.progress = intake.getPercentage()
    }

    override fun showAddIntakeDialog() {
        val amounts = arrayOf("250ml", "500ml", "750ml", "1000ml")
        val values = intArrayOf(250, 500, 750, 1000)

        AlertDialog.Builder(this)
            .setTitle("Add Water Intake")
            .setItems(amounts) { _, which ->
                presenter.addWaterIntake(values[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
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