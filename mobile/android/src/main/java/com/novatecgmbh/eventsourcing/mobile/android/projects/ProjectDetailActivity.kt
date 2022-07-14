package com.novatecgmbh.eventsourcing.mobile.android.projects

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.novatecgmbh.eventsourcing.mobile.Constants
import com.novatecgmbh.eventsourcing.mobile.android.R
import com.novatecgmbh.eventsourcing.mobile.android.projects.fragments.ProjectGraphqlFragment
import com.novatecgmbh.eventsourcing.mobile.android.projects.fragments.ProjectRestFragment

class ProjectDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_detail)

        val extras = intent.extras
        val projectId = extras?.getString(Constants.projectIdKey)

        val projectGraphqlFragment = ProjectGraphqlFragment.newInstance(projectId)
        val projectRestFragment = ProjectRestFragment.newInstance(projectId)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.content, projectGraphqlFragment)
        fragmentTransaction.commit()

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigation)
        bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.graphql_page -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.content, projectGraphqlFragment)
                    fragmentTransaction.commit()
                    true
                }
                else -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.content, projectRestFragment)
                    fragmentTransaction.commit()
                    true
                }
            }
        }
    }
}