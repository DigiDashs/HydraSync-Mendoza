package com.example.hydrasync.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class LoginRepository private constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var INSTANCE: LoginRepository? = null

        fun getInstance(): LoginRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LoginRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Convert FirebaseUser to our User model
                val names = firebaseUser.displayName?.split(" ") ?: listOf("", "")
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    firstName = names.getOrNull(0) ?: "",
                    lastName = names.getOrNull(1) ?: ""
                )
                LoginResponse(true, "Login successful", user)
            } else {
                LoginResponse(false, "Authentication failed")
            }
        } catch (e: Exception) {
            LoginResponse(false, getFirebaseErrorMessage(e))
        }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let { user ->
            val names = user.displayName?.split(" ") ?: listOf("", "")
            User(
                uid = user.uid,
                email = user.email ?: "",
                firstName = names.getOrNull(0) ?: "",
                lastName = names.getOrNull(1) ?: ""
            )
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    private fun getFirebaseErrorMessage(exception: Exception): String {
        return when (exception.message) {
            "The email address is badly formatted." -> "Invalid email format"
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "User not found"
            "The password is invalid or the user does not have a password." -> "Invalid password"
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Network error. Please check your connection"
            else -> "Login failed: ${exception.message}"
        }
    }

    // removed methods
    // - getAllUsers()
    // - registerUser()
    // - userCredentials map
    // - registeredUsers list
}