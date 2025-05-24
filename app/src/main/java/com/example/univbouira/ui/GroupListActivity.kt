package com.example.univbouira.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univbouira.adapters.GroupListAdapter
import com.example.univbouira.databinding.ActivityGroupListBinding
import com.example.univbouira.models.GroupItem
import com.google.firebase.firestore.FirebaseFirestore

class GroupListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupListBinding
    private lateinit var adapter: GroupListAdapter
    private val db = FirebaseFirestore.getInstance()

    private var moduleCode: String? = null
    private var moduleTitle: String? = null
    private var level: String? = null  // New: level param

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moduleCode = intent.getStringExtra("moduleCode")
        moduleTitle = intent.getStringExtra("moduleTitle")
        level = intent.getStringExtra("level") ?: "L3" // Default to L3 if missing

        if (moduleCode == null || moduleTitle == null) {
            Toast.makeText(this, "Module information missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Display module title below the header
        binding.moduleTitleText.text = "Groups - $moduleTitle ($level)"

        setupRecyclerView()
        loadGroups()
    }

    private fun setupRecyclerView() {
        adapter = GroupListAdapter(emptyList()) { selectedGroup ->
            val intent = Intent(this, StudentListActivity::class.java).apply {
                putExtra("moduleCode", moduleCode)
                putExtra("moduleTitle", moduleTitle)
                putExtra("groupName", selectedGroup.groupName)
                putExtra("level", level)  // Pass level downstream if needed
            }
            startActivity(intent)
        }

        binding.groupRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupRecyclerView.adapter = adapter
    }

    private fun loadGroups() {
        showLoading(true)
        val level = intent.getStringExtra("level") ?: "L3"
        Log.d("GroupListActivity", "Loading groups for level='$level'")

        db.collection("groups")
            .whereEqualTo("level", level)
            .get()
            .addOnSuccessListener { snapshot ->
                val groups = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id.trim()
                    if (id.isEmpty()) {
                        Log.w("GroupListActivity", "Skipping blank-ID doc at path=${doc.reference.path}")
                        null
                    } else {
                        Log.d("GroupListActivity", "Adding group id='$id'")
                        GroupItem(groupName = id)
                    }
                }

                showLoading(false)
                Log.d("GroupListActivity", "Final groups (${groups.size}): ${groups.joinToString { it.groupName }}")

                if (groups.isEmpty()) {
                    binding.groupRecyclerView.visibility = View.GONE
                    binding.emptyGroupMessage.visibility = View.VISIBLE
                } else {
                    adapter.updateGroups(groups)
                    binding.groupRecyclerView.visibility = View.VISIBLE
                    binding.emptyGroupMessage.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("GroupListActivity", "Error loading groups", e)
                Toast.makeText(this, "Failed to load groups", Toast.LENGTH_SHORT).show()
            }
    }



    private fun showLoading(loading: Boolean) {
        binding.groupLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
