package com.example.hydrasync.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.hydrasync.R
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity(), ProfileContract.View {

    private lateinit var presenter: ProfileContract.Presenter

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var tilBirthday: TextInputLayout
    private lateinit var etBirthday: EditText
    private lateinit var btnSave: Button
    private lateinit var ivBack: ImageView

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        presenter = ProfilePresenter(this)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        spinnerGender = findViewById(R.id.spinnerGender)
        tilBirthday = findViewById(R.id.tilBirthday)
        etBirthday = findViewById(R.id.etBirthday)
        btnSave = findViewById(R.id.btnSaveProfile)
        ivBack = findViewById(R.id.ivBack)

        setupDatePicker()
        presenter.loadProfile()

        btnSave.setOnClickListener {
            val updatedProfile = Profile(
                firstName = etFirstName.text.toString(),
                lastName = etLastName.text.toString(),
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

    private fun setupDatePicker() {
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            updateBirthdayInView()
        }

        etBirthday.setOnClickListener {
            val currentDate = etBirthday.text.toString()
            if (currentDate.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    val date = sdf.parse(currentDate)
                    date?.let {
                        calendar.time = it
                    }
                } catch (e: Exception) {
                    calendar.time = Date()
                }
            }

            DatePickerDialog(
                this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateBirthdayInView() {
        val dateFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.US)
        etBirthday.setText(sdf.format(calendar.time))
    }

    override fun showProfile(profile: Profile) {
        tvUserName.text = "${profile.firstName} ${profile.lastName}"
        tvUserEmail.text = profile.email

        etFirstName.setText(profile.firstName)
        etLastName.setText(profile.lastName)
        etEmail.setText(profile.email)


        val genderArray = resources.getStringArray(R.array.gender_options)
        val genderIndex = genderArray.indexOf(profile.gender)
        if (genderIndex >= 0) spinnerGender.setSelection(genderIndex)

        if (profile.birthday.isNotEmpty()) {
            etBirthday.setText(profile.birthday)

            try {
                val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                val date = sdf.parse(profile.birthday)
                date?.let {
                    calendar.time = it
                }
            } catch (e: Exception) {

            }
        }
    }

    override fun showSaveSuccess() {
        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()

        tvUserName.text = "${etFirstName.text} ${etLastName.text}"
        tvUserEmail.text = etEmail.text.toString()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
