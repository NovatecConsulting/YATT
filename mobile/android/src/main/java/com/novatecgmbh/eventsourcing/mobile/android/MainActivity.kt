package com.novatecgmbh.eventsourcing.mobile.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.novatecgmbh.eventsourcing.mobile.android.profile.ProfileFragment
import com.novatecgmbh.eventsourcing.mobile.android.projects.fragments.ProjectsOverviewFragment
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class MainActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val projectsOverviewFragment = ProjectsOverviewFragment()
        val profileFragment = ProfileFragment()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.content, projectsOverviewFragment)
        fragmentTransaction.commit()

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigation)
        bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.projects_page -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.content, projectsOverviewFragment)
                    fragmentTransaction.commit()
                    true
                }
                else -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.content, profileFragment)
                    fragmentTransaction.commit()
                    true
                }
            }
        }
    }
}