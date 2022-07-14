package com.novatecgmbh.eventsourcing.mobile.android.projects.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.novatecgmbh.eventsourcing.mobile.Constants
import com.novatecgmbh.eventsourcing.mobile.android.R
import com.novatecgmbh.eventsourcing.mobile.android.projects.CreateProjectActivity
import com.novatecgmbh.eventsourcing.mobile.android.projects.ProjectDetailActivity
import com.novatecgmbh.eventsourcing.mobile.android.ui.ProjectsRecyclerViewAdapter
import com.novatecgmbh.eventsourcing.mobile.graphQl.GraphQlClient
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.core.scope.Scope


class ProjectsOverviewFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScope()
    private val graphQlClient: GraphQlClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_projects_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ProjectsRecyclerViewAdapter()
        adapter.setOnItemClickListener(object: ProjectsRecyclerViewAdapter.ItemClickListener{
            override fun onItemClick(position: Int) {
                val intent = Intent(activity, ProjectDetailActivity::class.java)
                intent.putExtra(Constants.projectIdKey, adapter.dataSet[position]?.identifier)
                startActivity(intent)
            }
        })
        val recyclerView: RecyclerView = view.findViewById(R.id.projects_recycler)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val fab: FloatingActionButton = view.findViewById(R.id.add_project_fab)
        fab.setOnClickListener {
            val intent = Intent(activity, CreateProjectActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            val projects = graphQlClient.getProjects()
            adapter.addItems(projects)
        }

    }
}