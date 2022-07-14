package com.novatecgmbh.eventsourcing.mobile.android.projects

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.novatecgmbh.eventsourcing.mobile.android.MainActivity
import com.novatecgmbh.eventsourcing.mobile.android.R
import com.novatecgmbh.eventsourcing.mobile.android.ui.CompaniesSpinnerAdapter
import com.novatecgmbh.eventsourcing.mobile.graphQl.GraphQlClient
import de.novatec_gmbh.graphql_kmm.apollo.CompaniesQuery
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class CreateProjectActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()
    private val graphQlClient: GraphQlClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_project)

        val projectNameInput: EditText = findViewById(R.id.project_name_input)
        val startDateInput: EditText = findViewById(R.id.start_date_input)
        val deadlineInput: EditText = findViewById(R.id.deadline_input)
        val companySpinner: Spinner = findViewById(R.id.company_spinner)
        val createProjectButton: Button = findViewById(R.id.create_project_button)

        val adapter = CompaniesSpinnerAdapter(this, R.layout.support_simple_spinner_dropdown_item, mutableListOf())
        companySpinner.adapter = adapter


        val startDatePicker = DatePickerDialog(this)
        startDatePicker.setOnDateSetListener { datePicker, year, month, day ->
            startDateInput.setText(String.format("%04d-%02d-%02d",year,month+1,day))
        }
        startDateInput.setOnClickListener {
            startDatePicker.show()
        }

        val deadlinePicker = DatePickerDialog(this)
        deadlinePicker.setOnDateSetListener { datePicker, year, month, day ->
            deadlineInput.setText(String.format("%04d-%02d-%02d",year,month+1,day))
        }
        deadlineInput.setOnClickListener {
            deadlinePicker.show()
        }

        createProjectButton.setOnClickListener {
            lifecycleScope.launch {
                val projectCreated = graphQlClient.createProject(
                    projectNameInput.text.toString(),
                    startDateInput.text.toString(),
                    deadlineInput.text.toString(),
                    (companySpinner.selectedItem as CompaniesQuery.Company).identifier
                )
                if(projectCreated) {
                    val intent = Intent(this@CreateProjectActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        lifecycleScope.launch {
            adapter.addAll(graphQlClient.getCompanies())
        }
    }
}