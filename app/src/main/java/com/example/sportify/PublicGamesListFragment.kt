package com.example.sportify

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.adapter.PublicGameRecyclerAdapter
import com.example.sportify.databinding.FragmentPublicGamesListBinding
import com.example.sportify.model.Model
import com.example.sportify.model.Game
import com.example.sportify.model.Student

class PublicGamesListFragment : Fragment() {

    private var binding: FragmentPublicGamesListBinding? = null

    var games: List<Game>? = null
    var adapter: PublicGameRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPublicGamesListBinding.inflate(inflater, container, false)
        Log.d("DEBUG", "binding $binding")

        binding?.recyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        adapter = PublicGameRecyclerAdapter(games)
        adapter?.listener = object : OnGameClickListener {
            override fun onApprovalClicked(position: Int) {
                Log.d("TAG", "On click Activity listener on position $position")
                adapter?.onApprovalClicked(position)
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