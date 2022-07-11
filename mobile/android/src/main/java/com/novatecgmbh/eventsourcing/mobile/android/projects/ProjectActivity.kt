package com.novatecgmbh.eventsourcing.mobile.android.projects

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novatecgmbh.eventsourcing.mobile.Constants
import com.novatecgmbh.eventsourcing.mobile.android.R
import com.novatecgmbh.eventsourcing.mobile.android.ui.ParticipantsRecyclerViewAdapter
import com.novatecgmbh.eventsourcing.mobile.android.ui.TasksRecyclerViewAdapter
import com.novatecgmbh.eventsourcing.mobile.graphQl.GraphQlClient
import de.novatec_gmbh.graphql_kmm.apollo.type.ProjectStatus
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
        val extras = intent.extras
        val projectId = extras?.getString(Constants.projectIdKey)

        val projectNameText = findViewById<TextView>(R.id.project_heading)
        val projectIdText = findViewById<TextView>(R.id.project_id)
        val statusText = findViewById<TextView>(R.id.project_status)
        val startDateText = findViewById<TextView>(R.id.start_date)
        val endDateText = findViewById<TextView>(R.id.end_date)
        val participantsText = findViewById<TextView>(R.id.participants)
        val tasksText = findViewById<TextView>(R.id.tasks)
        val participantsRecycler = findViewById<RecyclerView>(R.id.participants_recycler)
        val tasksRecycler = findViewById<RecyclerView>(R.id.tasks_recycler)

        val participantsAdapter = ParticipantsRecyclerViewAdapter()
        participantsAdapter.setOnItemClickListener(object: ParticipantsRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(position: Int) {
                //TODO
            }
        })
        participantsRecycler.adapter = participantsAdapter
        participantsRecycler.layoutManager = LinearLayoutManager(this)

        val tasksAdapter = TasksRecyclerViewAdapter()
        tasksAdapter.setOnItemClickListener(object: TasksRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(position: Int) {
                //TODO
            }
        })
        tasksRecycler.adapter = tasksAdapter
        tasksRecycler.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            if(projectId != null) {
                val project = graphQlClient.getProject(projectId)

                projectNameText.text = project?.name
                projectIdText.text = project?.identifier
                statusText.text = project?.status.toString()
                if(project?.status == ProjectStatus.DELAYED) {
                    statusText.setTextColor(getColor(R.color.red))
                }
                else {
                    statusText.setTextColor(getColor(R.color.green))
                }
                startDateText.text = project?.startDate
                endDateText.text = project?.actualEndDate
                participantsText.text = project?.participants?.size.toString()
                tasksText.text = project?.tasks?.size.toString()

                participantsAdapter.addItems(project?.participants ?: mutableListOf())
                tasksAdapter.addItems(project?.tasks ?: mutableListOf())
            }
        }
    }
}