package com.example.sportify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.sportify.databinding.FragmentForgotPasswordBinding
import com.example.sportify.model.AuthManager

class ForgotPasswordFragment : Fragment() {

    private var binding: FragmentForgotPasswordBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)

        setupClickListeners()

        return binding?.root
    }

    private fun setupClickListeners() {
        binding?.resetButton?.setOnClickListener {
            sendResetEmail()
        }

        binding?.backToLoginText?.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
    }

    private fun sendResetEmail() {
        val email = binding?.emailEditText?.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        AuthManager.shared.sendPasswordResetEmail(email) { success, message ->
            showLoading(false)

            if (success) {
                Toast.makeText(context, "Password reset link sent to your email", Toast.LENGTH_LONG).show()
                Navigation.findNavController(requireView()).popBackStack()
            } else {
                Toast.makeText(context, message ?: "Failed to send reset email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding?.resetButton?.isEnabled = !isLoading
        binding?.emailEditText?.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}