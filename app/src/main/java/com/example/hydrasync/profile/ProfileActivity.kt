package com.example.hydrasync.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var btnEdit: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var llEditButtons: LinearLayout
    private lateinit var ivBack: ImageView

    private val calendar = Calendar.getInstance()
    private var isEditMode = false
    private var originalProfile: Profile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupClickListeners()
        presenter = ProfilePresenter(this)
        presenter.loadProfile()
    }

    private fun initializeViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        spinnerGender = findViewById(R.id.spinnerGender)
        tilBirthday = findViewById(R.id.tilBirthday)
        etBirthday = findViewById(R.id.etBirthday)
        btnEdit = findViewById(R.id.btnEdit)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        llEditButtons = findViewById(R.id.llEditButtons)
        ivBack = findViewById(R.id.ivBack)
    }

    private fun setupClickListeners() {
        btnEdit.setOnClickListener {
            enterEditMode()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnCancel.setOnClickListener {
            exitEditMode()
            // Restore original values
            originalProfile?.let { profile ->
                showProfile(profile)
            }
        }

        ivBack.setOnClickListener {
            if (isEditMode) {
                // If in edit mode, cancel first
                exitEditMode()
                originalProfile?.let { profile ->
                    showProfile(profile)
                }
            } else {
                finish()
            }
        }

        setupDatePicker()
    }

    private fun setupDatePicker() {
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            updateBirthdayInView()
        }

        etBirthday.setOnClickListener {
            if (isEditMode) {
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
    }

    private fun updateBirthdayInView() {
        val dateFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.US)
        etBirthday.setText(sdf.format(calendar.time))
    }

    private fun enterEditMode() {
        isEditMode = true
        originalProfile = getCurrentProfile()

        // Enable all fields with proper background
        setEditTextEditable(etFirstName, true)
        setEditTextEditable(etLastName, true)
        setEditTextEditable(etEmail, true)

        spinnerGender.isEnabled = true
        spinnerGender.background = ContextCompat.getDrawable(this, android.R.drawable.edit_text)

        tilBirthday.isEnabled = true
        setEditTextEditable(etBirthday, true)

        // Show save/cancel buttons, hide edit button
        llEditButtons.visibility = LinearLayout.VISIBLE
        btnEdit.visibility = Button.GONE
    }

    private fun exitEditMode() {
        isEditMode = false

        // Disable all fields with locked background
        setEditTextEditable(etFirstName, false)
        setEditTextEditable(etLastName, false)
        setEditTextEditable(etEmail, false)

        spinnerGender.isEnabled = false
        spinnerGender.background = ContextCompat.getDrawable(this, R.drawable.edit_text_locked)

        tilBirthday.isEnabled = false
        setEditTextEditable(etBirthday, false)

        // Show edit button, hide save/cancel buttons
        llEditButtons.visibility = LinearLayout.GONE
        btnEdit.visibility = Button.VISIBLE
    }

    private fun setEditTextEditable(editText: EditText, editable: Boolean) {
        editText.isFocusable = editable
        editText.isFocusableInTouchMode = editable
        editText.isClickable = editable
        editText.setCursorVisible(editable)

        if (editable) {
            editText.background = ContextCompat.getDrawable(this, android.R.drawable.edit_text)
        } else {
            editText.background = ContextCompat.getDrawable(this, R.drawable.edit_text_locked)
        }
    }

    private fun getCurrentProfile(): Profile {
        return Profile(
            firstName = etFirstName.text.toString(),
            lastName = etLastName.text.toString(),
            email = etEmail.text.toString(),
            gender = spinnerGender.selectedItem.toString(),
            birthday = etBirthday.text.toString()
        )
    }

    private fun saveProfile() {
        val updatedProfile = getCurrentProfile()
        presenter.saveProfile(updatedProfile)
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
                // Handle parsing error
            }
        }

        exitEditMode()
    }

    override fun showSaveSuccess() {
        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()

        // Update displayed name and email
        tvUserName.text = "${etFirstName.text} ${etLastName.text}"
        tvUserEmail.text = etEmail.text.toString()

        exitEditMode()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (isEditMode) {
            // If in edit mode, cancel first
            exitEditMode()
            originalProfile?.let { profile ->
                showProfile(profile)
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}