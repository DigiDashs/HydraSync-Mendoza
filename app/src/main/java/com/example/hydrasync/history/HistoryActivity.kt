package com.example.hydrasync.history

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hydrasync.R
import com.example.hydrasync.settings.SettingsActivity
import com.example.hydrasync.history.DrinkEntry
import com.example.hydrasync.history.HistoryView
import com.example.hydrasync.history.HistoryPresenter
import com.example.hydrasync.home.HomeActivity

class HistoryActivity : AppCompatActivity(), HistoryView {
    private lateinit var presenter: HistoryPresenter
    private lateinit var historyContainer: LinearLayout
    private lateinit var tvTodayTotal: TextView
    private lateinit var tvYesterdayTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize views by finding them in the XML layout
        historyContainer = findViewById(R.id.todayHistoryContainer)
        tvTodayTotal = findViewById(R.id.tvTodayTotal)
        tvYesterdayTotal = findViewById(R.id.tvYesterdayTotal)

        // Initialize the Presenter and link it to this View
        presenter = HistoryPresenter(this)
        presenter.loadDrinkHistory()

        setupBottomNavigation()
    }

    override fun showHistory(historyItems: List<DrinkEntry>) {
        // Clear any existing views to prevent duplicates
        historyContainer.removeAllViews()

        // Loop through the data and dynamically create UI elements for each entry
        historyItems.forEach { entry ->
            val entryView = layoutInflater.inflate(R.layout.history_entry_item, historyContainer, false) as LinearLayout
            val timeTextView = entryView.findViewById<TextView>(R.id.tvTime)
            val amountTextView = entryView.findViewById<TextView>(R.id.tvAmount)

            // Set the text from the data
            timeTextView.text = entry.time
            amountTextView.text = entry.amount

            // Add the new view to the container in the main layout
            historyContainer.addView(entryView)
        }
    }

    override fun setTodayTotal(total: String) {
        tvTodayTotal.text = "Today: $total L"
    }

    override fun setYesterdayTotal(total: String) {
        tvYesterdayTotal.text = "Yesterday: $total L"
    }

    private fun setupBottomNavigation() {
        val btnHome = findViewById<LinearLayout>(R.id.tabHome)
        val btnSettings = findViewById<LinearLayout>(R.id.tabSettings)

        btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
            }

        }
    }

