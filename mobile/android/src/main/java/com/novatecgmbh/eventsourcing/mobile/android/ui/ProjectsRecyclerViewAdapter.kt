package com.novatecgmbh.eventsourcing.mobile.android.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novatecgmbh.eventsourcing.mobile.android.R
import com.novatecgmbh.eventsourcing.mobile.android.projects.ProjectActivity
import de.novatec_gmbh.graphql_kmm.apollo.ProjectsQuery

class ProjectsRecyclerViewAdapter(val dataSet: MutableList<ProjectsQuery.Project?> = mutableListOf()): RecyclerView.Adapter<ProjectsRecyclerViewAdapter.ViewHolder>() {

    private lateinit var listener: ItemClickListener

    interface ItemClickListener {

        fun onItemClick(position: Int)

    }

    fun setOnItemClickListener(listener: ItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(view: View, listener: ItemClickListener) : RecyclerView.ViewHolder(view) {
        val projectNameText: TextView
        val projectIdText: TextView

        init {
            projectNameText = view.findViewById(R.id.project_name)
            projectIdText = view.findViewById(R.id.project_id)
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    fun addItems(items: List<ProjectsQuery.Project?>) {
        dataSet.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.projects_recycler_item, viewGroup, false)
        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.projectNameText.text = dataSet[position]?.name
        viewHolder.projectIdText.text = "ID: " + dataSet[position]?.identifier
    }

    override fun getItemCount() = dataSet.size
}