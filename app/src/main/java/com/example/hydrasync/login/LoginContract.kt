package com.example.hydrasync.login

interface LoginContract {
    interface View {
        fun showEmailError(error: String)
        fun showPasswordError(error: String)
        fun clearErrors()
        fun showLoginSuccess()
        fun showLoginError(error: String)
        fun showLoading(show: Boolean)
        fun navigateToHome()
        fun navigateToRegister()
    }

    interface Presenter {
        fun login(email: String, password: String)
        fun onRegisterClicked()
        fun onDestroy()
    }
}