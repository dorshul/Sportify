package com.example.sportify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.sportify.databinding.FragmentRegisterBinding
import com.example.sportify.model.AuthManager
import com.example.sportify.model.dao.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {

    private var binding: FragmentRegisterBinding? = null
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        setupClickListeners()

        return binding?.root
    }



    private fun setupClickListeners() {
        binding?.registerButton?.setOnClickListener {
            attemptRegister()
        }

        binding?.loginText?.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
    }

    private fun attemptRegister() {
        val name = binding?.nameEditText?.text.toString().trim()
        val email = binding?.emailEditText?.text.toString().trim()
        val password = binding?.passwordEditText?.text.toString()
        val ageText = binding?.ageEditText?.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || ageText.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageText.toIntOrNull()
        if (age == null || age <= 0) {
            Toast.makeText(context, "Please enter a valid age", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        AuthManager.shared.signUp(email, password) { success, message ->
            if (success) {
                // Create user profile in Firestore
                saveUserProfile(name, age)
            } else {
                showLoading(false)
                Toast.makeText(context, message ?: "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Add this method to ensure a complete user profile is created at registration
    private fun saveUserProfile(name: String, age: Int) {
        val userId = AuthManager.shared.userId

        val user = User(
            id = userId,
            name = name,
            age = age,
            profileImageUrl = "" // Explicitly initialize to empty string
        )

        db.collection("users").document(userId).set(user.toMap())
            .addOnSuccessListener {
                showLoading(false)
                navigateToMainApp()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(context, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMainApp() {
        Navigation.findNavController(requireView()).navigate(R.id.action_registerFragment_to_publicGamesListFragment)
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding?.registerButton?.isEnabled = !isLoading
        binding?.nameEditText?.isEnabled = !isLoading
        binding?.emailEditText?.isEnabled = !isLoading
        binding?.passwordEditText?.isEnabled = !isLoading
        binding?.ageEditText?.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}