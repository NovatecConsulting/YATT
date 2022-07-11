package com.novatecgmbh.eventsourcing.mobile.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novatecgmbh.eventsourcing.mobile.android.R
import de.novatec_gmbh.graphql_kmm.apollo.ProjectQuery

class ParticipantsRecyclerViewAdapter(val dataSet: MutableList<ProjectQuery.Participant?> = mutableListOf()): RecyclerView.Adapter<ParticipantsRecyclerViewAdapter.ViewHolder>() {
    private lateinit var listener: ItemClickListener

    interface ItemClickListener {

        fun onItemClick(position: Int)

    }

    fun setOnItemClickListener(listener: ItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(view: View, listener: ItemClickListener) : RecyclerView.ViewHolder(view) {
        val userNameText: TextView
        val participantIdText: TextView

        init {
            userNameText = view.findViewById(R.id.user_name)
            participantIdText = view.findViewById(R.id.participant_id)
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    fun addItems(items: List<ProjectQuery.Participant?>) {
        dataSet.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.project_participants_recycler_item, viewGroup, false)
        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.userNameText.text =
            "${dataSet[position]?.user?.firstname} ${dataSet[position]?.user?.lastname}"
        viewHolder.participantIdText.text = "ID: " + dataSet[position]?.identifier
    }

    override fun getItemCount() = dataSet.size
}