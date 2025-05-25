package com.example.univbouira.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.models.GroupItem

class GroupListAdapter(
    private var groups: List<GroupItem>,
    private val onGroupClick: (GroupItem) -> Unit
) : RecyclerView.Adapter<GroupListAdapter.GroupViewHolder>() {

    companion object {
        private const val TAG = "GroupListAdapter"
    }

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupNameText: TextView = itemView.findViewById(R.id.groupNameText)

        fun bind(group: GroupItem, isSelected: Boolean) {
            // Display only the group name without level prefix (e.g., "GROUPE 01")
            groupNameText.text = group.groupName
            itemView.isSelected = isSelected
            itemView.setBackgroundColor(
                if (isSelected)
                    itemView.context.getColor(R.color.teal_200)
                else
                    itemView.context.getColor(android.R.color.transparent)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        val isSelected = position == selectedPosition
        holder.bind(group, isSelected)

        holder.itemView.setOnClickListener {
            Log.d(TAG, "Clicked group[${position}]: '${group.groupName}' (ID: ${group.groupId})")

            // Update selection
            val previous = selectedPosition
            selectedPosition = position
            if (previous != RecyclerView.NO_POSITION) {
                notifyItemChanged(previous)
            }
            notifyItemChanged(selectedPosition)

            // Fire callback
            onGroupClick(group)
        }
    }

    override fun getItemCount(): Int = groups.size

    fun updateGroups(newGroups: List<GroupItem>) {
        groups = newGroups
        selectedPosition = RecyclerView.NO_POSITION
        Log.d(TAG, "Adapter now has ${groups.size} groups: ${groups.joinToString { "${it.groupName} (${it.groupId})" }}")
        notifyDataSetChanged()
    }
}