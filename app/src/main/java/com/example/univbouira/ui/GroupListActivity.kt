package com.example.univbouira.ui

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moduleCode = intent.getStringExtra("moduleCode")
        moduleTitle = intent.getStringExtra("moduleTitle")

        if (moduleCode == null || moduleTitle == null) {
            Toast.makeText(this, "Module information missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Display module title below the header
        binding.moduleTitleText.text = "Groups - $moduleTitle"

        setupRecyclerView()
        loadGroups()
    }

    private fun setupRecyclerView() {
        adapter = GroupListAdapter(emptyList()) { selectedGroup ->
            val intent = Intent(this, StudentListActivity::class.java).apply {
                putExtra("moduleCode", moduleCode)
                putExtra("moduleTitle", moduleTitle)
                putExtra("groupName", selectedGroup.groupName)
            }
            startActivity(intent)
        }

        binding.groupRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupRecyclerView.adapter = adapter
    }

    private fun loadGroups() {
        showLoading(true)

        db.collection("groups")
            .whereArrayContains("modules", moduleCode!!)
            .get()
            .addOnSuccessListener { result ->
                val groups = result.mapNotNull { doc ->
                    val name = doc.getString("name")
                    if (name != null) GroupItem(name) else null
                }

                showLoading(false)

                if (groups.isEmpty()) {
                    binding.groupRecyclerView.visibility = View.GONE
                    binding.emptyGroupMessage.visibility = View.VISIBLE
                } else {
                    adapter.updateGroups(groups)
                    binding.groupRecyclerView.visibility = View.VISIBLE
                    binding.emptyGroupMessage.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Failed to load groups", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(loading: Boolean) {
        binding.groupLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }
}