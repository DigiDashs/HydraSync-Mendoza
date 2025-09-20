package com.example.hydrasync.login

import kotlinx.coroutines.*

class LoginPresenter(private var view: LoginContract.View?) : LoginContract.Presenter {

    private val repository = LoginRepository.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun login(email: String, password: String) {
        view?.clearErrors()

        var hasError = false

        // Email validation
        if (email.isBlank()) {
            view?.showEmailError("Email is required")
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view?.showEmailError("Enter a valid email address")
            hasError = true
        }

        // Password validation
        if (password.isBlank()) {
            view?.showPasswordError("Password is required")
            hasError = true
        } else if (password.length < 6) {
            view?.showPasswordError("Password must be at least 6 characters")
            hasError = true
        }

        if (hasError) return

        view?.showLoading(true)

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.login(email, password)
                }

                view?.showLoading(false)

                if (response.isSuccess) {
                    view?.showLoginSuccess()
                    view?.navigateToHome()
                } else {
                    view?.showLoginError(response.message)
                }
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showLoginError("Login failed. Please try again.")
            }
        }
    }

    override fun onRegisterClicked() {
        view?.navigateToRegister()
    }

    override fun onDestroy() {
        scope.cancel()
        view = null
    }
}