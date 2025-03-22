package com.example.sportify

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.sportify.databinding.FragmentSplashBinding
import com.example.sportify.model.AuthManager

class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(inflater, container, false)

        // Delay for splash screen effect (2 seconds)
        handler.postDelayed({
            checkAuthenticationStatus()
        }, 2000)

        return binding?.root
    }

    private fun checkAuthenticationStatus() {
        if (AuthManager.shared.isSignedIn) {
            // User is already signed in, navigate to main content
            Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_publicGamesListFragment)
        } else {
            // User is not signed in, navigate to login
            Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        binding = null
    }
}