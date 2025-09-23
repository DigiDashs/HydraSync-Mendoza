package com.example.hydrasync.profile
class ProfilePresenter(
    private var view: ProfileContract.View?
) : ProfileContract.Presenter {

    private var profile: Profile? = null

    override fun loadProfile() {
        // TODO: Replace with real data source (DB, API, SharedPreferences)
        profile = Profile(
            name = "John Doe",
            email = "johndoe@email.com",
            gender = "Male",
            birthday = "01/01/1990"
        )
        profile?.let { view?.showProfile(it) }
    }

    override fun saveProfile(profile: Profile) {
        // TODO: Persist profile to DB or API
        this.profile = profile
        view?.showSaveSuccess()
    }

    override fun onDestroy() {
        view = null
    }
}
