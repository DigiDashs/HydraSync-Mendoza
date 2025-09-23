package com.example.hydrasync.profile

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.hydrasync.R

class ProfileActivity : AppCompatActivity(), ProfileContract.View {

    private lateinit var presenter: ProfileContract.Presenter

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etBirthday: EditText
    private lateinit var btnSave: Button
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        presenter = ProfilePresenter(this)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        spinnerGender = findViewById(R.id.spinnerGender)
        etBirthday = findViewById(R.id.etBirthday)
        btnSave = findViewById(R.id.btnSaveProfile)
        ivBack = findViewById(R.id.ivBack)

        presenter.loadProfile()

        btnSave.setOnClickListener {
            val updatedProfile = Profile(
                name = etName.text.toString(),
                email = etEmail.text.toString(),
                gender = spinnerGender.selectedItem.toString(),
                birthday = etBirthday.text.toString()
            )
            presenter.saveProfile(updatedProfile)
        }
        ivBack.setOnClickListener {
            finish()
        }
    }


    override fun showProfile(profile: Profile) {
        etName.setText(profile.name)
        etEmail.setText(profile.email)

        // Set spinner selection
        val genderArray = resources.getStringArray(R.array.gender_options)
        val genderIndex = genderArray.indexOf(profile.gender)
        if (genderIndex >= 0) spinnerGender.setSelection(genderIndex)

        etBirthday.setText(profile.birthday)
    }

    override fun showSaveSuccess() {
        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
