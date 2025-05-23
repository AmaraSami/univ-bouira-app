package com.example.univbouira.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.models.GroupItem

class GroupListAdapter(
    private var groups: List<GroupItem>,
    private val onGroupClick: (GroupItem) -> Unit
) : RecyclerView.Adapter<GroupListAdapter.GroupViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupNameText: TextView = itemView.findViewById(R.id.groupNameText)

        fun bind(group: GroupItem, isSelected: Boolean) {
            groupNameText.text = group.groupName
            itemView.isSelected = isSelected
            itemView.setBackgroundColor(
                if (isSelected)
                    itemView.context.getColor(R.color.teal_200)  // example selected color
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
            val adapterPos = holder.adapterPosition
            if (adapterPos == RecyclerView.NO_POSITION) return@setOnClickListener

            if (selectedPosition != adapterPos) {
                val previousPosition = selectedPosition
                selectedPosition = adapterPos
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                Toast.makeText(holder.itemView.context, "Selected: ${group.groupName}", Toast.LENGTH_SHORT).show()
                onGroupClick(group)
            }
        }
    }

    override fun getItemCount(): Int = groups.size

    fun updateGroups(newGroups: List<GroupItem>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
