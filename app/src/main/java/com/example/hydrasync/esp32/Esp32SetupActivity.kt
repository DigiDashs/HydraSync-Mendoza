package com.example.hydrasync.esp32

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hydrasync.R

class Esp32SetupActivity : AppCompatActivity(), Esp32Contract.View {

    private lateinit var presenter: Esp32Contract.Presenter
    private lateinit var tvStatus: TextView
    private lateinit var etSSID: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnConnect: Button
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_esp32_setup)

        tvStatus = findViewById(R.id.tvStatus)
        etSSID = findViewById(R.id.etSSID)
        etPassword = findViewById(R.id.etPassword)
        btnConnect = findViewById(R.id.btnConnect)
        ivBack = findViewById(R.id.ivBack)

        presenter = Esp32Presenter(this)

        btnConnect.setOnClickListener {
            val ssid = etSSID.text.toString()
            val password = etPassword.text.toString()
            if (ssid.isNotEmpty() && password.isNotEmpty()) {
                presenter.connectToEsp32(ssid, password)
            } else {
                showToast("Please enter both SSID and password")
            }
        }

        ivBack.setOnClickListener { finish() }
    }

    override fun showStatus(online: Boolean) {
        tvStatus.text = if (online) "Status: Online" else "Status: Offline"
        tvStatus.setTextColor(
            if (online) getColor(android.R.color.holo_green_dark)
            else getColor(android.R.color.holo_red_dark)
        )
    }

    override fun showToast(message: String) {
        tvStatus.text = message
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
