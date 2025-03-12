package com.example.sportify

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.sportify.databinding.FragmentProfileBinding
import com.example.sportify.model.AuthManager
import com.example.sportify.model.CloudinaryModel
import com.example.sportify.model.dao.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import android.util.Log

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    var previousBitmap: Bitmap? = null
    private val db = Firebase.firestore
    private var user: User? = null
    private val cloudinaryModel = CloudinaryModel()
    private val TAG = "ProfileFragment"
    private var isUploadingImage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        setupCameraLauncher()
        setupClickListeners()
        fetchUserProfile()

        return binding?.root
    }

    private fun setupCameraLauncher() {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.avatar)
        previousBitmap = (drawable as BitmapDrawable).bitmap
        cameraLauncher = registerForActivityResult((ActivityResultContracts.TakePicturePreview())) { bitmap ->
            if (bitmap != null) {
                // Update the ImageView with the new image and save it as the previous image
                previousBitmap = bitmap
                binding?.userPicture?.setImageBitmap(bitmap)

                // Upload the new profile picture
                uploadProfilePicture(bitmap)
            } else {
                // Camera was closed, restore the previous image
                binding?.userPicture?.setImageBitmap(previousBitmap)
            }
        }
    }

    private fun uploadProfilePicture(bitmap: Bitmap) {
        if (isUploadingImage) return

        val userId = AuthManager.shared.userId
        if (userId.isEmpty()) {
            Toast.makeText(context, "You must be logged in to update your profile", Toast.LENGTH_SHORT).show()
            return
        }

        isUploadingImage = true
        // Show a loading indicator
        Toast.makeText(context, "Uploading profile picture...", Toast.LENGTH_SHORT).show()

        cloudinaryModel.uploadImage(
            bitmap = bitmap,
            gameId = "profile_${userId}", // Use unique ID for profiles
            onSuccess = { imageUrl ->
                isUploadingImage = false
                if (!imageUrl.isNullOrBlank()) {
                    // Update the user object with the new image URL
                    user = user?.copy(profileImageUrl = imageUrl)

                    // Update Firestore
                    updateUserField("profileImageUrl", imageUrl)
                    Log.d(TAG, "Profile picture uploaded successfully: $imageUrl")

                    // Show success message
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onError = { error ->
                isUploadingImage = false
                Log.e(TAG, "Error uploading profile picture: $error")
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error uploading profile picture: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupClickListeners() {
        binding?.editUserPictureButton?.setOnClickListener {
            cameraLauncher?.launch(null)
        }

        binding?.nameEditButton?.setOnClickListener {
            if (binding?.nameEditText?.visibility == View.GONE) {
                // Switch to EditText
                binding?.nameEditText?.setText(binding?.nameTextValue?.text)
                binding?.nameEditText?.visibility = View.VISIBLE
                binding?.nameTextValue?.visibility = View.GONE
                binding?.nameEditButton?.setImageResource(R.drawable.ic_save) // Change to save icon
            } else {
                // Save changes and switch back to TextView
                val newName = binding?.nameEditText?.text?.toString() ?: ""
                binding?.nameTextValue?.text = newName
                binding?.nameEditText?.visibility = View.GONE
                binding?.nameTextValue?.visibility = View.VISIBLE
                binding?.nameEditButton?.setImageResource(R.drawable.ic_edit) // Change back to edit icon

                // Update name in Firestore
                updateUserField("name", newName)
            }
        }

        binding?.ageEditButton?.setOnClickListener {
            if (binding?.ageEditText?.visibility == View.GONE) {
                // Switch to EditText
                binding?.ageEditText?.setText(binding?.ageTextValue?.text)
                binding?.ageEditText?.visibility = View.VISIBLE
                binding?.ageTextValue?.visibility = View.GONE
                binding?.ageEditButton?.setImageResource(R.drawable.ic_save) // Change to save icon
            } else {
                val newAge = binding?.ageEditText?.text?.toString()
                binding?.ageTextValue?.text = newAge
                binding?.ageEditText?.visibility = View.GONE
                binding?.ageTextValue?.visibility = View.VISIBLE
                binding?.ageEditButton?.setImageResource(R.drawable.ic_edit) // Change back to edit icon

                updateUserField("age", newAge?.toIntOrNull() ?: 0)
            }
        }

        binding?.profileFragmentLogoutButton?.setOnClickListener {
            logout()
        }
    }

    private fun fetchUserProfile() {
        val userId = AuthManager.shared.userId
        if (userId.isEmpty()) {
            navigateToLogin()
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    user = User.fromMap(document.data ?: mapOf(), userId)
                    updateUI()

                    // Log the profile data to debug
                    Log.d(TAG, "Fetched user profile: ${user}")
                    Log.d(TAG, "Profile image URL: ${user?.profileImageUrl}")
                } else {
                    Log.e(TAG, "User document doesn't exist")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading profile: ${e.message}")
                Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI() {
        binding?.nameTextValue?.text = user?.name ?: ""
        binding?.ageTextValue?.text = user?.age?.toString() ?: ""

        if (!user?.profileImageUrl.isNullOrEmpty()) {
            Log.d(TAG, "Loading profile image from URL: ${user?.profileImageUrl}")
            Picasso.get()
                .load(user?.profileImageUrl)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(binding?.userPicture, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        Log.d(TAG, "Profile image loaded successfully")
                    }

                    override fun onError(e: Exception?) {
                        Log.e(TAG, "Error loading profile image: ${e?.message}")
                    }
                })
        } else {
            Log.d(TAG, "No profile image URL available")
            binding?.userPicture?.setImageResource(R.drawable.avatar)
        }
    }

    private fun updateUserField(field: String, value: Any) {
        val userId = AuthManager.shared.userId
        if (userId.isEmpty()) return

        db.collection("users").document(userId)
            .update(field, value)
            .addOnSuccessListener {
                Log.d(TAG, "User field $field updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update profile: ${e.message}")
                Toast.makeText(context, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logout() {
        AuthManager.shared.signOut()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val navController = Navigation.findNavController(requireActivity(), R.id.main_nav_host)
        navController.navigate(R.id.loginFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}