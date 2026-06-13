package com.studentprofile.app.presentation.screens.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.studentprofile.app.presentation.viewmodel.AuthViewModel
import com.studentprofile.app.presentation.viewmodel.AuthState

class AuthComposeFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                val authState by authViewModel.authState.collectAsState()

                when (val state = authState) {
                    is AuthState.SubdomainRequired -> SubdomainScreen(authViewModel)
                    is AuthState.Unauthenticated -> LoginScreen(authViewModel)
                    is AuthState.StudentSelectionRequired -> StudentSelectionScreen(state.parentId, state.students, authViewModel)
                    is AuthState.MPINRegistrationRequired -> MPINRegistrationScreen(state.studentId, authViewModel)
                    is AuthState.MPINLoginRequired -> MPINLoginScreen(state.studentId, authViewModel)
                    is AuthState.Error -> {
                        // Normally we stay on the current screen and show an error.
                        // For simplicity, if we get a top-level error state, we show Login.
                        LoginScreen(authViewModel)
                    }
                    else -> {
                        // Authenticated state is handled by MainActivity to navigate to Dashboard
                    }
                }
            }
        }
    }
}
