package com.example.sportify

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportify.adapter.PublicGamesRecyclerAdapter
import com.example.sportify.databinding.FragmentPublicGamesListBinding
import com.example.sportify.model.Model
import com.example.sportify.model.Game

class PublicGamesListFragment : Fragment() {

    private var binding: FragmentPublicGamesListBinding? = null

    private var viewModel: PublicGamesListViewModel? = null
    private var adapter: PublicGamesRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPublicGamesListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PublicGamesListViewModel::class.java]

        binding?.recyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        adapter = PublicGamesRecyclerAdapter(viewModel?.games)
        adapter?.listener = object : OnPublicGameClickListener {
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
            viewModel?.games = it
            adapter?.update(it)
            adapter?.notifyDataSetChanged()
            binding?.progressBar?.visibility = View.GONE
        }
    }
}