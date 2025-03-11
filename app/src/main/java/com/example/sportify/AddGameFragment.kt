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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.sportify.databinding.FragmentAddGameBinding
import com.example.sportify.model.AuthManager
import com.example.sportify.model.Model
import com.example.sportify.model.Game
import com.example.sportify.model.WeatherService
import com.squareup.picasso.Picasso
import java.util.UUID

class AddGameFragment : Fragment() {
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var binding: FragmentAddGameBinding? = null
    var game: Game? = null
    var previousBitmap: Bitmap? = null
    private var didSetGameImage = false
    private val weatherService = WeatherService()
    private var isSaving = false
    private var originalLocation: String? = null

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
                didSetGameImage = true
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
        if (gameId != null) {
            Model.shared.getGameById(gameId) {
                game = it
                // Store the original location to check for changes later
                originalLocation = it?.location

                binding?.descriptionText?.text = Editable.Factory.getInstance().newEditable(game?.description ?: "")
                binding?.locationText?.text = Editable.Factory.getInstance().newEditable(game?.location ?: "")
                binding?.numberOfPlayers?.text = Editable.Factory.getInstance().newEditable(game?.numberOfPlayers.toString() ?: "")
                game?.pictureUrl?.let {
                    if (it.isNotBlank()) {
                        Picasso.get()
                            .load(it)
                            .placeholder(R.drawable.take_picture)
                            .into(binding?.takePictureImageView)
                    }
                }
            }
        }

        return binding?.root
    }

    private fun onSaveClicked(view: View) {
        if (isSaving) {
            return // Prevent multiple clicks
        }

        if (AuthManager.shared.userId.isEmpty()) {
            Toast.makeText(context, "You must be logged in to create games", Toast.LENGTH_SHORT).show()
            return
        }

        val location = binding?.locationText?.text?.toString()?.trim() ?: ""
        val locationChanged = location != originalLocation && game != null

        // Show loading state
        isSaving = true
        binding?.saveButton?.isEnabled = false
        binding?.saveButton?.text = "Saving..."

        // Check if location is valid
        val isValidLocation = weatherService.isValidLocation(location)

        // If location changed or is invalid, clear the weather data
        val shouldClearWeather = locationChanged || (location.isNotEmpty() && !isValidLocation)

        // Create updated game object
        val updatedGame = Game(
            id = game?.id ?: UUID.randomUUID().toString(),
            userId = game?.userId ?: AuthManager.shared.userId,
            pictureUrl = game?.pictureUrl ?: "@drawable/take_picture",
            approvals = game?.approvals ?: 0,
            location = location,
            description = binding?.descriptionText?.text?.toString() ?: "",
            numberOfPlayers = binding?.numberOfPlayers?.text?.toString()?.toIntOrNull() ?: 0,
            isApproved = game?.isApproved ?: false,
            // Only keep weather data if location didn't change and is valid
            weatherTemp = if (shouldClearWeather) null else game?.weatherTemp,
            weatherDescription = if (shouldClearWeather) null else game?.weatherDescription,
            weatherIcon = if (shouldClearWeather) null else game?.weatherIcon
        )

        // Update the game reference
        game = updatedGame

        // If location changed, let's invalidate the cache for the old location
        if (locationChanged && originalLocation?.isNotEmpty() == true) {
            WeatherService.removeFromCache(originalLocation!!)
        }

        // If location is invalid but not empty, show a warning
        if (location.isNotEmpty() && !isValidLocation) {
            Toast.makeText(context, "Location may be invalid. Weather data might not be available.", Toast.LENGTH_SHORT).show()
        }

        // First save the game
        saveGameWithImageAndWeather(view, location, locationChanged, isValidLocation)
    }


    private fun saveGameWithImageAndWeather(view: View, location: String, locationChanged: Boolean, isValidLocation: Boolean) {
        val bitmap = if (didSetGameImage) {
            binding?.takePictureImageView?.isDrawingCacheEnabled = true
            binding?.takePictureImageView?.buildDrawingCache()
            (binding?.takePictureImageView?.drawable as BitmapDrawable).bitmap
        } else null

        Model.shared.addGame(game!!, bitmap) {
            // Only try to fetch weather if location exists, is valid, and either changed or missing weather data
            if (location.isNotEmpty() && isValidLocation &&
                (locationChanged || game?.weatherTemp.isNullOrEmpty())) {

                Model.shared.fetchWeatherForGame(game!!, forceRefresh = locationChanged) { weatherSuccess ->
                    if (!weatherSuccess) {
                        // If weather fetch failed, make sure data is completely cleared
                        Model.shared.clearWeatherData(game!!) { _ ->
                            activity?.runOnUiThread {
                                isSaving = false
                                binding?.saveButton?.isEnabled = true
                                binding?.saveButton?.text = "Save"
                                // Show toast message about weather being unavailable
                                Toast.makeText(context, "Weather unavailable for this location", Toast.LENGTH_SHORT).show()
                                Navigation.findNavController(view).popBackStack()
                            }
                        }
                    } else {
                        activity?.runOnUiThread {
                            isSaving = false
                            binding?.saveButton?.isEnabled = true
                            binding?.saveButton?.text = "Save"
                            Navigation.findNavController(view).popBackStack()
                        }
                    }
                }
            } else if (location.isNotEmpty() && !isValidLocation) {
                // For invalid locations, explicitly clear weather data
                Model.shared.clearWeatherData(game!!) { _ ->
                    activity?.runOnUiThread {
                        isSaving = false
                        binding?.saveButton?.isEnabled = true
                        binding?.saveButton?.text = "Save"
                        Navigation.findNavController(view).popBackStack()
                    }
                }
            } else {
                activity?.runOnUiThread {
                    isSaving = false
                    binding?.saveButton?.isEnabled = true
                    binding?.saveButton?.text = "Save"
                    Navigation.findNavController(view).popBackStack()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}