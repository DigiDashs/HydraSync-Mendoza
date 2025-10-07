package com.example.hydrasync.profile

import com.example.hydrasync.data.UserRepository
import kotlinx.coroutines.*
import android.util.Log

class ProfilePresenter(
    private var view: ProfileContract.View?
) : ProfileContract.Presenter {

    private val userRepository = UserRepository.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun loadProfile() {
        scope.launch {
            try {
                Log.d("ProfilePresenter", "Loading profile from Firebase...")
                val user = withContext(Dispatchers.IO) {
                    userRepository.getUserProfile()
                }

                if (user != null) {
                    Log.d("ProfilePresenter", "User loaded: ${user.firstName} ${user.lastName}")
                    val profile = Profile(
                        firstName = user.firstName,    // Use separate fields
                        lastName = user.lastName,
                        email = user.email,
                        gender = user.gender,
                        birthday = user.birthday
                    )
                    view?.showProfile(profile)
                } else {
                    Log.e("ProfilePresenter", "No user profile found in Firebase")
                    view?.showError("Failed to load profile - no user data found")
                }
            } catch (e: Exception) {
                Log.e("ProfilePresenter", "Error loading profile: ${e.message}", e)
                view?.showError("Error loading profile: ${e.message}")
            }
        }
    }

    override fun saveProfile(profile: Profile) {
        scope.launch {
            try {

                val currentUser = withContext(Dispatchers.IO) {
                    userRepository.getUserProfile()
                }

                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        firstName = profile.firstName,
                        lastName = profile.lastName,
                        gender = profile.gender,
                        birthday = profile.birthday
                    )

                    val success = withContext(Dispatchers.IO) {
                        userRepository.updateUserProfile(updatedUser)
                    }

                    if (success) {
                        Log.d("ProfilePresenter", "Profile saved successfully")
                        view?.showSaveSuccess()
                    } else {
                        Log.e("ProfilePresenter", "Failed to save profile to Firebase")
                        view?.showError("Failed to save profile")
                    }
                } else {
                    Log.e("ProfilePresenter", "No current user found")
                    view?.showError("User not found")
                }
            } catch (e: Exception) {
                Log.e("ProfilePresenter", "Error saving profile: ${e.message}", e)
                view?.showError("Error saving profile: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        view = null
    }
}
