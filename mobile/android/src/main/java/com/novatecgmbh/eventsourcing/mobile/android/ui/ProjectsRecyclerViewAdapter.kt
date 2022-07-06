package com.novatecgmbh.eventsourcing.mobile.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novatecgmbh.eventsourcing.mobile.android.R
import de.novatec_gmbh.graphql_kmm.apollo.ProjectsQuery

class ProjectsRecyclerViewAdapter(private val dataSet: MutableList<ProjectsQuery.Project?> = mutableListOf()): RecyclerView.Adapter<ProjectsRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectNameText: TextView
        val projectIdText: TextView

        init {
            // Define click listener for the ViewHolder's View.
            projectNameText = view.findViewById(R.id.project_name)
            projectIdText = view.findViewById(R.id.project_id)
        }
    }

    fun addItems(items: List<ProjectsQuery.Project?>) {
        dataSet.addAll(items)
        notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.projects_recycler_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.projectNameText.text = dataSet[position]?.name
        viewHolder.projectIdText.text = "ID: " + dataSet[position]?.identifier
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}