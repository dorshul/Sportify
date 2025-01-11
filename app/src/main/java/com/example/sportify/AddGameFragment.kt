package com.example.sportify

import android.os.Bundle
import android.text.Editable
import android.util.Log
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
    var game: Game? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddGameBinding.inflate(inflater, container, false)
        binding?.saveButton?.setOnClickListener(::onSaveClicked)
        val gameId = arguments?.let { AddGameFragmentArgs.fromBundle(it).gameId }
        if (gameId != null) {
            Model.shared.getGameById(gameId ?: "") {
                game = it
                Log.d("DEBUG", "gameId $gameId")
                binding?.descriptionText?.text = Editable.Factory.getInstance().newEditable(game?.description ?: "")
                binding?.locationText?.text = Editable.Factory.getInstance().newEditable(game?.location ?: "")
                binding?.numberOfPlayers?.text = Editable.Factory.getInstance().newEditable(game?.numberOfPlayers.toString() ?: "")
            }
        }

        return binding?.root
    }

    private fun onSaveClicked(view: View) {
        game = Game(
            id = game?.id ?: UUID.randomUUID().toString(),
            userId =  game?.userId ?: UUID.randomUUID().toString(),
            pictureUrl = game?.pictureUrl ?:"@drawable/take_picture",
            approvals = game?.approvals ?: 0,
            location = binding?.locationText?.text?.toString() ?: "",
            description = binding?.descriptionText?.text?.toString() ?: "",
            numberOfPlayers = binding?.numberOfPlayers?.text?.toString()?.toIntOrNull() ?: 0,
            isApproved = game?.isApproved ?: false,
        )

        Model.shared.addGame(game!!) {
            Navigation.findNavController(view).popBackStack()
        }
    }
    override fun onResume() {
        super.onResume()
        getGame()
    }

    private fun getGame() {

    }

}