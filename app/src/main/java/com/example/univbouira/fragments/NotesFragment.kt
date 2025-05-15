package com.example.univbouira.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.ModuleItem
import com.example.univbouira.R
import com.example.univbouira.adapters.NotesAdapter


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
