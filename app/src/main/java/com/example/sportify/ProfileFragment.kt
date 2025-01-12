package com.example.sportify

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sportify.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

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