package com.novatecgmbh.eventsourcing.mobile.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novatecgmbh.eventsourcing.mobile.android.R
import com.novatecgmbh.eventsourcing.mobile.data.projects.TaskResource
import de.novatec_gmbh.graphql_kmm.apollo.ProjectQuery

class TasksRecyclerViewAdapter(val dataSet: MutableList<TaskResource?> = mutableListOf()): RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder>() {
    private lateinit var listener: ItemClickListener

    interface ItemClickListener {

        fun onItemClick(position: Int)

    }

    fun setOnItemClickListener(listener: ItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(view: View, listener: ItemClickListener) : RecyclerView.ViewHolder(view) {
        val taskNameText: TextView
        val taskDateRangeText: TextView
        val taskStatusText: TextView

        init {
            taskNameText = view.findViewById(R.id.task_name)
            taskDateRangeText = view.findViewById(R.id.task_date_range)
            taskStatusText = view.findViewById(R.id.task_status)
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    fun addItems(items: List<TaskResource?>) {
        dataSet.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.project_tasks_recycler_item, viewGroup, false)
        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.taskNameText.text = dataSet[position]?.name
        viewHolder.taskDateRangeText.text = "${dataSet[position]?.startDate} - ${dataSet[position]?.endDate}"
        viewHolder.taskStatusText.text = dataSet[position]?.status.toString()
    }

    override fun getItemCount() = dataSet.size
}