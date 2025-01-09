package com.example.sportify

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.sportify.databinding.FragmentAddGameBinding
import com.example.sportify.model.Model
import com.example.sportify.model.Game
import java.util.UUID

class AddGameFragment : Fragment() {
    private var binding: FragmentAddGameBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddGameBinding.inflate(inflater, container, false)
        binding?.saveButton?.setOnClickListener(::onSaveClicked)
        return binding?.root
    }

    private fun onSaveClicked(view: View) {
        val game = Game(
            id = UUID.randomUUID().toString(),
            userId = UUID.randomUUID().toString(),
            pictureUrl = "@drawable/take_picture",
            approvals = 0,
            location = binding?.locationText?.text?.toString() ?: "",
            description = binding?.descriptionText?.text?.toString() ?: "",
            numberOfPlayers = binding?.numberOfPlayers?.text?.toString()?.toIntOrNull() ?: 0,
            isApproved = false
        )

        Model.shared.addGame(game) {
            Navigation.findNavController(view).popBackStack()
        }
    }
}