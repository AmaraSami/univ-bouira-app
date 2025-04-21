package com.example.myapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ModuleItem
import com.example.myapplication.R
import com.example.myapplication.adapters.NotesAdapter


class NotesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val itemList = listOf(
            ModuleItem("IHM", "All about UI/UX Design", "13.5"),
            ModuleItem("Android Dev", "Create real apps", "17.0"),
            ModuleItem("Networking", "Learn about TCP/IP", "15.5")
        )

        adapter = NotesAdapter(itemList)
        recyclerView.adapter = adapter

        return view
    }
}
