package com.novatecgmbh.eventsourcing.mobile.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.novatecgmbh.eventsourcing.mobile.android.projects.ProjectsActivity
import com.novatecgmbh.eventsourcing.mobile.domain.AuthRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class LoginActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()

    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val userNameInput: EditText = findViewById(R.id.username_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val loginButton: Button = findViewById(R.id.login_button)

        loginButton.setOnClickListener {
            lifecycleScope.launch {
                authRepository.login(userNameInput.text.toString(), passwordInput.text.toString())
                val intent = Intent(this@LoginActivity, ProjectsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
