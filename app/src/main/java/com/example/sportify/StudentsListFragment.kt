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
import com.example.sportify.adapter.StudentsRecyclerAdapter
import com.example.sportify.databinding.FragmentStudentsListBinding
import com.example.sportify.model.Model
import com.example.sportify.model.Student


class StudentsListFragment : Fragment() {

    private var binding: FragmentStudentsListBinding? = null

    var students: List<Student>? = null
    var adapter: StudentsRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStudentsListBinding.inflate(inflater, container, false)
        Log.d("DEBUG", "binding $binding")
        // TODO: Integrate students in fragment ✅
        // TODO: Refactor Model ✅
        // TODO: Save new student ✅
        // TODO: Reloading data list ✅

        binding?.recyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        adapter = StudentsRecyclerAdapter(students)
        adapter?.listener = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                Log.d("TAG", "On click Activity listener on position $position")
            }

            override fun onItemClick(student: Student?) {
                // TODO
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
        getAllStudents()
    }

    private fun getAllStudents() {

        binding?.progressBar?.visibility = View.VISIBLE
        Model.shared.getAllStudents {
            students = it
            adapter?.update(students)
            adapter?.notifyDataSetChanged()

            binding?.progressBar?.visibility = View.GONE
        }
    }
}