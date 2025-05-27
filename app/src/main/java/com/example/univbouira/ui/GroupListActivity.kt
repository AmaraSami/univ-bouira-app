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
    private var level: String? = null

    companion object {
        private const val TAG = "GroupListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moduleCode = intent.getStringExtra("moduleCode")
        moduleTitle = intent.getStringExtra("moduleTitle")
        level = intent.getStringExtra("level") ?: "L3"

        if (moduleCode == null || moduleTitle == null) {
            Toast.makeText(this, "Module information missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.moduleTitleText.text = "Groups - $moduleTitle ($level)"

        setupRecyclerView()
        loadGroups()
    }

    private fun setupRecyclerView() {
        adapter = GroupListAdapter(emptyList()) { selectedGroup ->
            val intent = Intent(this, StudentListActivity::class.java).apply {
                putExtra("moduleCode", moduleCode)
                putExtra("moduleTitle", moduleTitle)
                putExtra("groupName", selectedGroup.documentId) // Pass full ID (e.g., "L3_GROUPE_01")
                putExtra("level", level)
            }
            startActivity(intent)
        }

        binding.groupRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupRecyclerView.adapter = adapter
    }

    private fun loadGroups() {
        showLoading(true)
        val currentLevel = level ?: "L3"
        Log.d(TAG, "Loading groups for level='$currentLevel'")

        // Load groups from the database
        // Groups are stored as documents in "groups" collection with names like "L3_GROUPE_01"
        db.collection("groups")
            .get()
            .addOnSuccessListener { snapshot ->
                val groups = snapshot.documents.mapNotNull { doc ->
                    val docId = doc.id.trim()
                    if (docId.isEmpty()) {
                        Log.w(TAG, "Skipping blank-ID doc at path=${doc.reference.path}")
                        null
                    } else {
                        Log.d(TAG, "Processing group with document ID='$docId'")

                        // Create GroupItem from document ID using the companion function
                        val groupItem = GroupItem.fromDocumentId(docId)

                        // Filter groups by level - only include groups that match the current level
                        if (groupItem.level == currentLevel) {
                            Log.d(TAG, "Adding group: ${groupItem.groupName} (ID: ${groupItem.documentId})")
                            groupItem
                        } else {
                            Log.d(TAG, "Skipping group ${groupItem.groupName} - wrong level (${groupItem.level} != $currentLevel)")
                            null
                        }
                    }
                }.sortedBy { it.getGroupNumberInt() } // Sort by group number

                showLoading(false)
                Log.d(TAG, "Final groups (${groups.size}): ${groups.joinToString { "${it.groupName} (${it.documentId})" }}")

                if (groups.isEmpty()) {
                    binding.groupRecyclerView.visibility = View.GONE
                    binding.emptyGroupMessage.visibility = View.VISIBLE
                    binding.emptyGroupMessage.text = "No groups found for $currentLevel"
                } else {
                    adapter.updateGroups(groups)
                    binding.groupRecyclerView.visibility = View.VISIBLE
                    binding.emptyGroupMessage.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e(TAG, "Error loading groups", e)
                Toast.makeText(this, "Failed to load groups: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(loading: Boolean) {
        binding.groupLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }
}