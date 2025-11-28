package com.example.hydrasync.esp32

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class Esp32Presenter(private val view: Esp32Contract.View) : Esp32Contract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var statusJob: Job? = null

    override fun connectToEsp32(ssid: String, password: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.4.1/setWifi")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val postData = "ssid=$ssid&password=$password"
                conn.outputStream.use { it.write(postData.toByteArray()) }

                if (conn.responseCode == 200) {
                    withContext(Dispatchers.Main) {
                        view.showToast("Credentials sent successfully!")
                        startStatusPolling()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        view.showToast("Failed to send credentials")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.showToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun startStatusPolling() {
        statusJob?.cancel()
        statusJob = scope.launch {
            while (isActive) {
                checkStatus()
                delay(2000)
            }
        }
    }

    private suspend fun checkStatus() {
        try {
            val url = URL("http://192.168.4.1/status")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"
            conn.connect()

            val online = conn.responseCode == 200
            withContext(Dispatchers.Main) {
                view.showStatus(online)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                view.showStatus(false)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        statusJob?.cancel()
    }
}
