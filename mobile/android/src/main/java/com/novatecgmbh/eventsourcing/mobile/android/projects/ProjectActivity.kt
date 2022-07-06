package com.novatecgmbh.eventsourcing.mobile.android.projects

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novatecgmbh.eventsourcing.mobile.android.R
import com.novatecgmbh.eventsourcing.mobile.android.ui.ProjectsRecyclerViewAdapter
import com.novatecgmbh.eventsourcing.mobile.graphQl.GraphQlClient
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class ProjectActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()
    private val graphQlClient: GraphQlClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        val adapter = ProjectsRecyclerViewAdapter()
        val recyclerView: RecyclerView = findViewById(R.id.projects_recycler)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            adapter.addItems(graphQlClient.getProjects())
            println(adapter.itemCount)
        }

    }
}