package com.novatecgmbh.eventsourcing.mobile.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class MainActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}