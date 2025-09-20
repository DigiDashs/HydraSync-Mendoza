package com.example.hydrasync.register

import com.example.hydrasync.login.LoginRepository
import com.example.hydrasync.login.User
import kotlinx.coroutines.*
import java.util.*

class RegisterPresenter(private var view: RegisterContract.View?) : RegisterContract.Presenter {

    private val repository = LoginRepository.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun register(firstName: String, lastName: String, email: String, password: String) {
        view?.clearErrors()

        var hasError = false

        // First name validation
        if (firstName.isBlank()) {
            view?.showFirstNameError("First name is required")
            hasError = true
        } else if (firstName.length < 2) {
            view?.showFirstNameError("First name must be at least 2 characters")
            hasError = true
        }

        // Last name validation
        if (lastName.isBlank()) {
            view?.showLastNameError("Last name is required")
            hasError = true
        } else if (lastName.length < 2) {
            view?.showLastNameError("Last name must be at least 2 characters")
            hasError = true
        }

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
                val success = withContext(Dispatchers.IO) {
                    val newUser = User(
                        id = UUID.randomUUID().toString(),
                        email = email,
                        firstName = firstName,
                        lastName = lastName
                    )
                    Thread.sleep(1500) // Simulate network delay
                    repository.registerUser(newUser, password)
                }

                view?.showLoading(false)

                if (success) {
                    view?.showRegistrationSuccess()
                    view?.navigateToLogin()
                } else {
                    view?.showRegistrationError("User with this email already exists")
                }
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showRegistrationError("Registration failed. Please try again.")
            }
        }
    }

    override fun onBackToLoginClicked() {
        view?.navigateToLogin()
    }

    override fun onDestroy() {
        scope.cancel()
        view = null
    }
}