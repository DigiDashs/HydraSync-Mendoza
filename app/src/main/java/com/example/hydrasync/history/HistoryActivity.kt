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
    private lateinit var historyContainer: LinearLayout // Changed from todayHistoryContainer
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
        presenter.loadDrinkHistory()
    }

    private fun initializeViews() {
        // Use the new historyContainer instead of todayHistoryContainer
        historyContainer = findViewById(R.id.historyContainer)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)

        progressBar = ProgressBar(this).apply {
            isVisible = false
        }
    }

    private fun setupPresenter() {
        presenter = HistoryPresenter(this)
        presenter.loadDrinkHistory()
    }

    // Updated HistoryView implementation
    override fun showHistoryByDate(historyByDate: Map<String, List<DrinkEntry>>) {
        historyContainer.removeAllViews()

        if (historyByDate.isEmpty()) {
            showEmptyState(true)
            return
        }

        showEmptyState(false)

        // Create a section for each date
        historyByDate.forEach { (date, entries) ->
            val dateSectionView = createDateSectionView(date, entries)
            historyContainer.addView(dateSectionView)
        }
    }

    override fun showEmptyState(show: Boolean) {
        emptyStateContainer.isVisible = show
        historyContainer.isVisible = !show
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

    // Create a section for a specific date
    private fun createDateSectionView(date: String, entries: List<DrinkEntry>): View {
        val sectionView = LayoutInflater.from(this)
            .inflate(R.layout.data_section_layout, null)

        val tvDate = sectionView.findViewById<TextView>(R.id.tvDate)
        val tvTotal = sectionView.findViewById<TextView>(R.id.tvTotal)
        val entriesContainer = sectionView.findViewById<LinearLayout>(R.id.entriesContainer)

        // Set date and calculate total
        tvDate.text = date
        val totalMl = entries.sumOf { it.amount.replace(" ML", "").toIntOrNull() ?: 0 }
        tvTotal.text = presenter.formatTotal(totalMl)

        // Populate entries
        populateEntriesContainer(entriesContainer, entries)

        return sectionView
    }

    private fun populateEntriesContainer(container: LinearLayout, entries: List<DrinkEntry>) {
        container.removeAllViews()

        entries.forEachIndexed { index, entry ->
            val entryView = createHistoryEntryView(entry)
            container.addView(entryView)

            if (index < entries.size - 1) {
                container.addView(createDividerView())
            }
        }
    }

    // Your existing methods remain the same
    private fun createHistoryEntryView(entry: DrinkEntry): View {
        val entryView = LayoutInflater.from(this)
            .inflate(R.layout.item_history_entry, null)

        val tvTime = entryView.findViewById<TextView>(R.id.tvTime)
        val tvAmount = entryView.findViewById<TextView>(R.id.tvAmount)

        tvTime.text = entry.time
        tvAmount.text = entry.amount

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

    // Your existing showEditDialog, showDeleteConfirmation, and navigation methods remain the same
    override fun showEditDialog(entry: DrinkEntry) {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}