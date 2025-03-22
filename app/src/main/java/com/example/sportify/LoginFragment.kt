package com.example.sportify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.sportify.databinding.FragmentLoginBinding
import com.example.sportify.model.AuthManager

class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Check if user is already signed in
        if (AuthManager.shared.isSignedIn) {
            navigateToMainApp()
            return binding?.root
        }

        setupClickListeners()

        return binding?.root
    }

    private fun setupClickListeners() {
        binding?.loginButton?.setOnClickListener {
            attemptLogin()
        }

        binding?.registerText?.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding?.forgotPasswordText?.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    private fun attemptLogin() {
        val email = binding?.emailEditText?.text.toString().trim()
        val password = binding?.passwordEditText?.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        AuthManager.shared.signIn(email, password) { success, message ->
            showLoading(false)

            if (success) {
                navigateToMainApp()
            } else {
                Toast.makeText(context, message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainApp() {
        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_publicGamesListFragment)
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding?.loginButton?.isEnabled = !isLoading
        binding?.emailEditText?.isEnabled = !isLoading
        binding?.passwordEditText?.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}