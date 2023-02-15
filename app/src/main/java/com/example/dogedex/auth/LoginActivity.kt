package com.example.dogedex.auth

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.dogedex.main.MainActivity
import com.example.dogedex.R
import com.example.dogedex.api.ApiResponseStatus
import com.example.dogedex.databinding.ActivityLoginBinding
import com.example.dogedex.model.User

class LoginActivity : AppCompatActivity(), LoginFragment.LoginFragmentActions,
    SignUpFragment.SignUpFragmentActions {

    private val viewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.status.observe(this) {
            when (it) {
                is ApiResponseStatus.Error -> {
                    binding.loadingWheel.visibility = View.GONE
                    showErrorDialog(it.messageId)
                }
                is ApiResponseStatus.Loading -> binding.loadingWheel.visibility = View.VISIBLE
                is ApiResponseStatus.Succcess -> binding.loadingWheel.visibility = View.GONE
            }
        }

        viewModel.user.observe(this) {
            if (it != null) {
                User.setLoggedInUser(this, it)
                startMainActivity()
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showErrorDialog(messageId:Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.there_was_an_error)
            .setMessage(messageId)
            .setPositiveButton(android.R.string.ok) {_, _ -> /** dimiss **/ }
            .create()
            .show()
    }

    override fun onRegisterButtonClick() {
        findNavController(R.id.nav_host_fragment)
            .navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
    }

    override fun onLoginFieldsValidated(email: String, password: String) {
        viewModel.login(email, password)
    }

    override fun onSignupFieldsValidated(
        email: String,
        password: String,
        passwordConfirmation: String
    ) {
        viewModel.signUp(email, password, passwordConfirmation)
    }
}