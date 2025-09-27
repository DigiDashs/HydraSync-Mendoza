package com.example.hydrasync.register

interface RegisterContract {
    interface View {
        fun showFirstNameError(error: String)
        fun showLastNameError(error: String)
        fun showEmailError(error: String)
        fun showPasswordError(error: String)
        fun clearErrors()
        fun showRegistrationSuccess()
        fun showRegistrationError(error: String)
        fun showLoading(show: Boolean)
        fun navigateToLogin()
    }

    interface Presenter {
        fun register(firstName: String, lastName: String, email: String, password: String, dob: String, gender: String)
        fun onBackToLoginClicked()
        fun onDestroy()
    }
}