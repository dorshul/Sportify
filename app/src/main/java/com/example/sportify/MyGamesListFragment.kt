package com.example.sportify

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportify.adapter.MyGamesRecyclerAdapter
import com.example.sportify.databinding.FragmentMyGamesListBinding
import com.example.sportify.model.Model
import com.example.sportify.model.Game

class MyGamesListFragment : Fragment() {

    private var binding: FragmentMyGamesListBinding? = null

    private val viewModel: MyGamesListViewModel by viewModels()
    private var adapter: MyGamesRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyGamesListBinding.inflate(inflater, container, false)

        binding?.recyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        adapter = MyGamesRecyclerAdapter(viewModel.games.value)
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

        adapter?.listener = object : OnMyGameClickListener {
            override fun onEditClick(game: Game?) {
                game?.let {
                    val action = MyGamesListFragmentDirections.actionMyGamesListFragmentToAddGameFragment(it.id)
                    binding?.root?.let {
                        Navigation.findNavController(it).navigate(action)
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
        Model.shared.refreshAllGames()
    }

    private fun getAllGames() {
        binding?.progressBar?.visibility = View.VISIBLE
        Model.shared.refreshAllGames()
    }
}