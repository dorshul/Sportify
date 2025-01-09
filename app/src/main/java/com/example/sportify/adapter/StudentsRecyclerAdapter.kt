package com.example.sportify.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sportify.OnItemClickListener
import com.example.sportify.R
import com.example.sportify.model.Student

class StudentsRecyclerAdapter(private var students: List<Student>?): RecyclerView.Adapter<StudentViewHolder>() {

        var listener: OnItemClickListener? = null

        fun update(students: List<Student>?) {
            this.students = students
        }

        override fun getItemCount(): Int = students?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.public_game_card,
                parent,
                false
            )
            return StudentViewHolder(itemView, listener)
        }

        override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
            holder.bind(
                student = students?.get(position),
                position = position
            )
        }
    }