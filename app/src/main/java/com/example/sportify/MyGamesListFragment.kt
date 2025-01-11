package com.example.sportify

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportify.adapter.MyGamesRecyclerAdapter
import com.example.sportify.databinding.FragmentMyGamesListBinding
import com.example.sportify.model.Model
import com.example.sportify.model.Game

class MyGamesListFragment : Fragment() {

    private var binding: FragmentMyGamesListBinding? = null

    var games: List<Game>? = null
    var adapter: MyGamesRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyGamesListBinding.inflate(inflater, container, false)

        binding?.recyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        adapter = MyGamesRecyclerAdapter(games)
        adapter?.listener = object : OnMyGameClickListener {
            override fun onEditClick(game: Game?) {
                game?.let {
                    val bundle = Bundle().apply {
                        putString("gameId", game.id) // Pass the argument dynamically
                    }
                    binding?.root?.let {
                        Navigation.findNavController(it).navigate(R.id.action_myGamesListFragment_to_addGameFragment, bundle)
                    }
                }
            }
        }
        binding?.recyclerView?.adapter = adapter

        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        getAllGames()
    }

    private fun getAllGames() {
        binding?.progressBar?.visibility = View.VISIBLE
        Model.shared.getAllGames {
            games = it
            adapter?.update(games)
            adapter?.notifyDataSetChanged()
            binding?.progressBar?.visibility = View.GONE
        }
    }
}