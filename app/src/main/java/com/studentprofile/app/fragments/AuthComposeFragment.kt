package com.studentprofile.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.studentprofile.app.AuthViewModel
import com.studentprofile.app.IntroScreen
import com.studentprofile.app.LoginScreen
import com.studentprofile.app.R
import com.studentprofile.app.Screen
import com.studentprofile.app.SignupScreen

class AuthComposeFragment : Fragment() {

    companion object {
        const val ARG_SCREEN = "screen"

        fun newInstance(screen: String): AuthComposeFragment {
            return AuthComposeFragment().apply {
                arguments = bundleOf(ARG_SCREEN to screen)
            }
        }
    }

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val screen = requireArguments().getString(ARG_SCREEN).orEmpty()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            val navController = findNavController()

            setContent {
                when (screen) {
                    Screen.Login.route -> LoginScreen(navController, authViewModel)
                    Screen.Signup.route -> SignupScreen(navController, authViewModel)
                    else -> IntroScreen(navController)
                }
            }
        }
    }
}