package com.example.hydrasync.esp32

interface Esp32Contract {

    interface View {
        fun showStatus(online: Boolean)
        fun showToast(message: String)
    }

    interface Presenter {
        fun connectToEsp32(ssid: String, password: String)
        fun onDestroy()
    }
}
