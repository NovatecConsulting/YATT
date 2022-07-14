package com.novatecgmbh.eventsourcing.mobile.android.projects.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novatecgmbh.eventsourcing.mobile.android.R
import com.novatecgmbh.eventsourcing.mobile.android.ui.ParticipantsRecyclerViewAdapter
import com.novatecgmbh.eventsourcing.mobile.android.ui.TasksRecyclerViewAdapter
import com.novatecgmbh.eventsourcing.mobile.data.projects.ProjectClient
import com.novatecgmbh.eventsourcing.mobile.data.projects.Status
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.core.scope.Scope

private const val ARG_PROJECT_ID = "projectId"

class ProjectRestFragment : Fragment(), AndroidScopeComponent {
    private var projectId: String? = null
    override val scope: Scope by fragmentScope()
    private val projectClient: ProjectClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            projectId = it.getString(ARG_PROJECT_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_project, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val projectNameText = view.findViewById<TextView>(R.id.project_heading)
        val projectIdText = view.findViewById<TextView>(R.id.project_id)
        val statusText = view.findViewById<TextView>(R.id.project_status)
        val startDateText = view.findViewById<TextView>(R.id.start_date)
        val endDateText = view.findViewById<TextView>(R.id.end_date)
        val participantsText = view.findViewById<TextView>(R.id.participants)
        val tasksText = view.findViewById<TextView>(R.id.tasks)
        val participantsRecycler = view.findViewById<RecyclerView>(R.id.participants_recycler)
        val tasksRecycler = view.findViewById<RecyclerView>(R.id.tasks_recycler)

        val participantsAdapter = ParticipantsRecyclerViewAdapter()
        participantsAdapter.setOnItemClickListener(object: ParticipantsRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(position: Int) {
                //TODO
            }
        })
        participantsRecycler.adapter = participantsAdapter
        participantsRecycler.layoutManager = LinearLayoutManager(activity)

        val tasksAdapter = TasksRecyclerViewAdapter()
        tasksAdapter.setOnItemClickListener(object: TasksRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(position: Int) {
                //TODO
            }
        })
        tasksRecycler.adapter = tasksAdapter
        tasksRecycler.layoutManager = LinearLayoutManager(activity)

        lifecycleScope.launch {
            if(projectId != null) {
                context?.let {
                    val startTime = System.currentTimeMillis()
                    val project = projectClient.getProject(projectId!!)
                    val participants = projectClient.getParticipants(projectId!!)
                    val tasks = projectClient.getTasks(projectId!!)
                    val endTime = System.currentTimeMillis()
                    println("Loading all project data with REST took ${endTime - startTime}ms")

                    projectNameText.text = project.name
                    projectIdText.text = project.identifier
                    statusText.text = project.status.toString()
                    if(project.status == Status.DELAYED) {
                        statusText?.setTextColor(ContextCompat.getColor(it, R.color.red))
                    }
                    else {
                        statusText?.setTextColor(ContextCompat.getColor(it, R.color.green))
                    }
                    startDateText.text = project.startDate
                    endDateText.text = project.actualEndDate
                    participantsText.text = participants.size.toString()
                    tasksText.text = tasks.size.toString()

                    participantsAdapter.addItems(participants)
                    tasksAdapter.addItems(tasks)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(projectId: String?) =
            ProjectRestFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                }
            }
    }
}