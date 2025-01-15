package com.example.sportify

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.sportify.databinding.FragmentAddGameBinding
import com.example.sportify.model.Model
import com.example.sportify.model.Game
import java.util.UUID

class AddGameFragment : Fragment() {
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var binding: FragmentAddGameBinding? = null
    var game: Game? = null
    var previousBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddGameBinding.inflate(inflater, container, false)
        binding?.saveButton?.setOnClickListener(::onSaveClicked)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.take_picture)
        previousBitmap = (drawable as BitmapDrawable).bitmap
        cameraLauncher = registerForActivityResult((ActivityResultContracts.TakePicturePreview())) { bitmap ->
            if (bitmap != null) {
                // Update the ImageView with the new image and save it as the previous image
                previousBitmap = bitmap
                binding?.takePictureImageView?.setImageBitmap(bitmap)
            } else {
                // Camera was closed, restore the previous image
                binding?.takePictureImageView?.setImageBitmap(previousBitmap)
            }
        }
        binding?.takePictureImageView?.setOnClickListener {
            cameraLauncher?.launch(null)
        }
        val args = AddGameFragmentArgs.fromBundle(requireArguments())
        val gameId = args.gameId
//        val gameId = arguments?.let { AddGameFragmentArgs.fromBundle(requireArguments()).gameId }
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
            pictureUrl = game?.pictureUrl ?: "@drawable/take_picture",
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
}