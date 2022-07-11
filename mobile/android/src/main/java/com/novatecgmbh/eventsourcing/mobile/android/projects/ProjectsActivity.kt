package com.novatecgmbh.eventsourcing.mobile.android.projects

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.novatecgmbh.eventsourcing.mobile.Constants
import com.novatecgmbh.eventsourcing.mobile.android.R
import com.novatecgmbh.eventsourcing.mobile.android.ui.ProjectsRecyclerViewAdapter
import com.novatecgmbh.eventsourcing.mobile.graphQl.GraphQlClient
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class ProjectsActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()
    private val graphQlClient: GraphQlClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        val adapter = ProjectsRecyclerViewAdapter()
        adapter.setOnItemClickListener(object: ProjectsRecyclerViewAdapter.ItemClickListener{
            override fun onItemClick(position: Int) {
                val intent = Intent(this@ProjectsActivity, ProjectActivity::class.java)
                intent.putExtra(Constants.projectIdKey, adapter.dataSet[position]?.identifier)
                startActivity(intent)
            }
        })
        val recyclerView: RecyclerView = findViewById(R.id.projects_recycler)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fab: FloatingActionButton = findViewById(R.id.add_project_fab)
        fab.setOnClickListener {
            val intent = Intent(this@ProjectsActivity, CreateProjectActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            adapter.addItems(graphQlClient.getProjects())
        }

    }
}