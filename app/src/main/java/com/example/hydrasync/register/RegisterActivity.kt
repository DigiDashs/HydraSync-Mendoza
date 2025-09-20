package com.example.hydrasync.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hydrasync.R
import com.example.hydrasync.login.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity(), RegisterContract.View {

    private lateinit var tilFirstName: TextInputLayout
    private lateinit var tilLastName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var presenter: RegisterPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        presenter = RegisterPresenter(this)
        setupClickListeners()
    }

    private fun initViews() {
        tilFirstName = findViewById(R.id.tilFirstName)
        tilLastName = findViewById(R.id.tilLastName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            presenter.register(firstName, lastName, email, password)
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