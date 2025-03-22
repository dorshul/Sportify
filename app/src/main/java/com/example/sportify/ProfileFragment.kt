package com.example.sportify

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.sportify.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    var previousBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.avatar)
        previousBitmap = (drawable as BitmapDrawable).bitmap
        cameraLauncher = registerForActivityResult((ActivityResultContracts.TakePicturePreview())) { bitmap ->
            if (bitmap != null) {
                // Update the ImageView with the new image and save it as the previous image
                previousBitmap = bitmap
                binding?.userPicture?.setImageBitmap(bitmap)
            } else {
                // Camera was closed, restore the previous image
                binding?.userPicture?.setImageBitmap(previousBitmap)
            }
        }
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
                binding?.nameTextValue?.text = binding?.nameEditText?.text.toString()
                binding?.nameEditText?.visibility = View.GONE
                binding?.nameTextValue?.visibility = View.VISIBLE
                binding?.nameEditButton?.setImageResource(R.drawable.ic_edit) // Change back to edit icon
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
                // Save changes and switch back to TextView
                binding?.ageTextValue?.text = binding?.ageEditText?.text.toString()
                binding?.ageEditText?.visibility = View.GONE
                binding?.ageTextValue?.visibility = View.VISIBLE
                binding?.ageEditButton?.setImageResource(R.drawable.ic_edit) // Change back to edit icon
            }
        }
        
        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}