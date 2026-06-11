package com.studentprofile.app

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged

class MainActivity : FragmentActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create ViewModel scoped to this Activity — survives config changes
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNav = findViewById(R.id.bottom_navigation)

        val startDestination = when (authViewModel.authState.value) {
            is AuthState.Authenticated -> R.id.nav_dashboard
            is AuthState.ParentAuthenticated -> R.id.nav_select_student
            else -> R.id.nav_intro
        }

        navController.graph = navController.navInflater
            .inflate(R.navigation.nav_graph)
            .apply {
                setStartDestination(startDestination)
            }


        bottomNav.setupWithNavController(navController)
        bottomNav.isVisible = authViewModel.authState.value is AuthState.Authenticated

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect  { state ->
                        when (state) {
                            is AuthState.Unauthenticated -> {
                                bottomNav.isVisible = false
                                navigateToDestination(R.id.nav_intro, popBackStack = true)
                            }

                            is AuthState.Authenticated -> {
                                bottomNav.isVisible = true
                                navigateToDestination(R.id.nav_dashboard, popBackStack = true)
                            }

                            is AuthState.ParentAuthenticated -> {
                                bottomNav.isVisible = false
                                navigateToDestination(R.id.nav_select_student, popBackStack = false)
                            }

                            is AuthState.Error -> {
                                // Let Compose screen show error.
                            }

                            AuthState.Loading -> {
                                // Do nothing for now.
                            }
                        }
                    }
            }
        }

    }

    private fun navigateToDestination(destinationId: Int, popBackStack: Boolean) {
        if (navController.currentDestination?.id == destinationId) {
            return
        }

        navController.navigate(destinationId) {
            launchSingleTop = true
            if (popBackStack) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }
}
