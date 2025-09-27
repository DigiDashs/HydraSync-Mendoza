package com.example.hydrasync.register

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hydrasync.R
import com.example.hydrasync.login.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

class RegisterActivity : AppCompatActivity(), RegisterContract.View {

    private lateinit var tilFirstName: TextInputLayout
    private lateinit var tilLastName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilDob: TextInputLayout
    private lateinit var tilGender: TextInputLayout

    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etDob: TextInputEditText
    private lateinit var etGender: AutoCompleteTextView

    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var presenter: RegisterPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        presenter = RegisterPresenter(this)

        setupDobPicker()
        setupGenderDropdown()
        setupClickListeners()
    }

    private fun initViews() {
        tilFirstName = findViewById(R.id.tilFirstName)
        tilLastName = findViewById(R.id.tilLastName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilDob = findViewById(R.id.tilDob)
        tilGender = findViewById(R.id.tilGender)

        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etDob = findViewById(R.id.etDob)
        etGender = findViewById(R.id.etGender)

        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupDobPicker() {
        etDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                etDob.setText("$d/${m + 1}/$y")
            }, year, month, day).show()
        }
    }

    private fun setupGenderDropdown() {
        val genderOptions = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            genderOptions
        )
        etGender.setAdapter(genderAdapter)

        // Disable keyboard, force dropdown only
        etGender.keyListener = null
        etGender.setOnClickListener {
            etGender.showDropDown()
        }
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val dob = etDob.text.toString().trim()
            val gender = etGender.text.toString().trim()

            presenter.register(firstName, lastName, email, password, dob, gender)
        }

        tvLogin.setOnClickListener {
            presenter.onBackToLoginClicked()
        }
    }

    override fun showFirstNameError(error: String) {
        tilFirstName.error = error
    }

    override fun showLastNameError(error: String) {
        tilLastName.error = error
    }

    override fun showEmailError(error: String) {
        tilEmail.error = error
    }

    override fun showPasswordError(error: String) {
        tilPassword.error = error
    }

    override fun clearErrors() {
        tilFirstName.error = null
        tilLastName.error = null
        tilEmail.error = null
        tilPassword.error = null
        tilDob.error = null
        tilGender.error = null
    }

    override fun showRegistrationSuccess() {
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
    }

    override fun showRegistrationError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !show
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
