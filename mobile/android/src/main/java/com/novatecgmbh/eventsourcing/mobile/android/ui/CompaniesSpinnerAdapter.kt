package com.novatecgmbh.eventsourcing.mobile.android.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.novatec_gmbh.graphql_kmm.apollo.CompaniesQuery


class CompaniesSpinnerAdapter(
    context: Context,
    resource: Int,
    private val values: MutableList<CompaniesQuery.Company>
) : ArrayAdapter<CompaniesQuery.Company>(context, resource, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label: TextView = super.getView(position, convertView, parent) as TextView
        label.text = values[position].name
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label: TextView = super.getDropDownView(position, convertView, parent) as TextView
        label.text = values[position].name
        return label
    }

}