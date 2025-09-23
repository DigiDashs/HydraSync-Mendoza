package com.example.hydrasync.profile
interface ProfileContract {

    interface View {
        fun showProfile(profile: Profile)
        fun showSaveSuccess()
        fun showError(message: String)
    }

    interface Presenter {
        fun loadProfile()
        fun saveProfile(profile: Profile)
        fun onDestroy()
    }
}
