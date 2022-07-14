package com.novatecgmbh.eventsourcing.mobile.android.profile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.novatecgmbh.eventsourcing.mobile.android.R
import com.novatecgmbh.eventsourcing.mobile.domain.UserRepository
import com.novatecgmbh.eventsourcing.mobile.graphQl.GraphQlClient
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.core.scope.Scope


class ProfileFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScope()
    private val userRepository: UserRepository by inject()
    private val graphQlClient: GraphQlClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val firstnameInput: TextInputEditText = view.findViewById(R.id.firstname_input)
        val lastnameInput: TextInputEditText = view.findViewById(R.id.lastname_input)
        val saveButton: Button = view.findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            firstnameInput.clearFocus()
            lastnameInput.clearFocus()
            lifecycleScope.launch {
                val renamed = graphQlClient.renameUser(
                    userRepository.current().id,
                    firstnameInput.text.toString(),
                    lastnameInput.text.toString()
                )
                if(renamed) {
                    Snackbar.make(view, "Successfully renamed", LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            userRepository.current().let {
                firstnameInput.setText(it.firstname)
                lastnameInput.setText(it.lastname)
            }
        }
    }
}