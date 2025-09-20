package com.example.hydrasync.login

class LoginRepository {

    companion object {
        @Volatile
        private var INSTANCE: LoginRepository? = null

        fun getInstance(): LoginRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LoginRepository().also { INSTANCE = it }
            }
        }
    }

    // Sample users for testing
    private val registeredUsers = mutableListOf(
        User("1", "admin@hydrasync.com", "Admin", "User"),
        User("2", "john@example.com", "John", "Doe")
    )

    private val userCredentials = mapOf(
        "admin@hydrasync.com" to "admin123",
        "john@example.com" to "password123"
    )

    private var currentUser: User? = null

    fun login(email: String, password: String): LoginResponse {
        // Simulate network delay
        Thread.sleep(1500)

        val storedPassword = userCredentials[email]

        return if (storedPassword == password) {
            currentUser = registeredUsers.find { it.email == email }
            LoginResponse(true, "Login successful", currentUser)
        } else {
            LoginResponse(false, "Invalid email or password")
        }
    }

    fun getCurrentUser(): User? = currentUser

    fun logout() {
        currentUser = null
    }

    fun getAllUsers(): List<User> = registeredUsers.toList()

    fun registerUser(user: User, password: String): Boolean {
        return if (registeredUsers.any { it.email == user.email }) {
            false // User already exists
        } else {
            registeredUsers.add(user)
            userCredentials.toMutableMap()[user.email] = password
            true
        }
    }
}