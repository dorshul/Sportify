package com.example.sportify

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportify.adapter.PublicGamesRecyclerAdapter
import com.example.sportify.databinding.FragmentPublicGamesListBinding
import com.example.sportify.model.Model
import com.example.sportify.model.Game

class PublicGamesListFragment : Fragment() {

    private var binding: FragmentPublicGamesListBinding? = null

    private val viewModel: PublicGamesListViewModel by viewModels()
    private var adapter: PublicGamesRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPublicGamesListBinding.inflate(inflater, container, false)

        binding?.recyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        adapter = PublicGamesRecyclerAdapter(viewModel.games.value)
        viewModel.games.observe(viewLifecycleOwner) {
            adapter?.update(it)
            adapter?.notifyDataSetChanged()
            binding?.progressBar?.visibility = View.GONE
        }
        binding?.swipeToRefresh?.setOnRefreshListener {
            viewModel.refreshAllGames()
        }
        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }

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
        viewModel.refreshAllGames()
    }
}