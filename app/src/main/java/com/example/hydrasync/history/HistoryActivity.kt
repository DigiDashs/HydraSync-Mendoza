package com.example.hydrasync.history

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.hydrasync.R
import com.example.hydrasync.home.HomeActivity
import com.example.hydrasync.profile.ProfileActivity
import com.example.hydrasync.settings.SettingsActivity

class HistoryActivity : AppCompatActivity(), HistoryView {

    private lateinit var presenter: HistoryPresenter

    // View references
    private lateinit var todayHistoryContainer: LinearLayout
    private lateinit var yesterdayHistoryContainer: LinearLayout
    private lateinit var tvTodayTotal: TextView
    private lateinit var tvYesterdayTotal: TextView
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        initializeViews()
        setupPresenter()
        setupBottomNavigation()
        setupProfileClick()
    }

    override fun onResume() {
        super.onResume()
        // Refresh history when returning from Home (after adding new entries)
        presenter.loadDrinkHistory()
    }

    private fun initializeViews() {
        todayHistoryContainer = findViewById(R.id.todayHistoryContainer)
        yesterdayHistoryContainer = findViewById(R.id.yesterdayHistoryContainer)
        tvTodayTotal = findViewById(R.id.tvTodayTotal)
        tvYesterdayTotal = findViewById(R.id.tvYesterdayTotal)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)

        progressBar = ProgressBar(this).apply {
            isVisible = false
        }
    }

    private fun setupPresenter() {
        presenter = HistoryPresenter(this)
        presenter.loadDrinkHistory()
    }

    // HistoryView interface implementations
    override fun showTodayHistory(entries: List<DrinkEntry>) {
        populateHistoryContainer(todayHistoryContainer, entries)
    }

    override fun showYesterdayHistory(entries: List<DrinkEntry>) {
        populateHistoryContainer(yesterdayHistoryContainer, entries)
    }

    override fun setTodayTotal(totalMl: Int) {
        tvTodayTotal.text = presenter.formatTotal(totalMl)
    }

    override fun setYesterdayTotal(totalMl: Int) {
        tvYesterdayTotal.text = presenter.formatTotal(totalMl)
    }

    override fun showEmptyState(show: Boolean) {
        emptyStateContainer.isVisible = show
    }

    override fun showLoadingState(show: Boolean) {
        progressBar.isVisible = show
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showEditDialog(entry: DrinkEntry) {
        // Extract the numeric amount from the string (e.g., "250 ML" -> 250)
        val currentAmount = entry.amount.replace(" ML", "").toIntOrNull() ?: 250

        val input = EditText(this).apply {
            setText(currentAmount.toString())
            hint = "Enter amount in ml"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            selectAll()
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Entry")
            .setMessage("Time: ${entry.time}")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newAmount = input.text.toString().toIntOrNull()
                if (newAmount != null && newAmount > 0 && newAmount <= 2000) {
                    presenter.editEntry(entry, newAmount)
                } else {
                    showError("Please enter a valid amount (1-2000ml)")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showDeleteConfirmation(entry: DrinkEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Delete ${entry.amount} intake at ${entry.time}?")
            .setPositiveButton("Delete") { _, _ ->
                presenter.deleteEntry(entry)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun populateHistoryContainer(container: LinearLayout, entries: List<DrinkEntry>) {
        container.removeAllViews()

        entries.forEachIndexed { index, entry ->
            val entryView = createHistoryEntryView(entry)
            container.addView(entryView)

            if (index < entries.size - 1) {
                container.addView(createDividerView())
            }
        }
    }

    private fun createHistoryEntryView(entry: DrinkEntry): View {
        val entryView = LayoutInflater.from(this)
            .inflate(R.layout.item_history_entry, null)

        val tvTime = entryView.findViewById<TextView>(R.id.tvTime)
        val tvAmount = entryView.findViewById<TextView>(R.id.tvAmount)

        // Use the existing properties from your DrinkEntry
        tvTime.text = entry.time
        tvAmount.text = entry.amount

        // Make entries interactive
        entryView.setOnClickListener {
            showEditDialog(entry)
        }

        entryView.setOnLongClickListener {
            showDeleteConfirmation(entry)
            true
        }

        return entryView
    }

    private fun createDividerView(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setBackgroundColor(resources.getColor(android.R.color.darker_gray, theme))
        }
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.tabHome).setOnClickListener {
            navigateToActivity(HomeActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.tabSettings).setOnClickListener {
            navigateToActivity(SettingsActivity::class.java)
        }
    }

    private fun setupProfileClick() {
        findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            navigateToActivity(ProfileActivity::class.java)
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
        // Don't call finish() so user can come back and see updated data
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}