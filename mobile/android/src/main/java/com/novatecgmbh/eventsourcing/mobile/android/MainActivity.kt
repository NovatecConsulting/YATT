package com.novatecgmbh.eventsourcing.mobile.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.novatecgmbh.eventsourcing.mobile.Greeting
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.novatecgmbh.eventsourcing.mobile.domain.AuthRepository
import com.novatecgmbh.eventsourcing.mobile.domain.UserRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

fun greet(): String {
    return Greeting().greeting()
}

class MainActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()

    private val authRepository: AuthRepository by inject()
    private val userRepository: UserRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv: TextView = findViewById(R.id.text_view)
        tv.text = greet()

        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            lifecycleScope.launch {
                authRepository.login("Test1", "test")
            }
        }
    }
}
