package com.studentprofile.app

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import com.studentprofile.app.presentation.viewmodel.AuthViewModel
import com.studentprofile.app.presentation.viewmodel.AuthState
import androidx.compose.ui.platform.ComposeView
import androidx.drawerlayout.widget.DrawerLayout
import com.studentprofile.app.ui.SchoolNavigationDrawer

class MainActivity : FragmentActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        setContentView(R.layout.activity_main)
        val composeDrawer = findViewById<ComposeView>(R.id.compose_drawer)

        composeDrawer.setContent {
            SchoolNavigationDrawer(
                studentName = "Yash Tiwari",
                classInfo = "Class 10 - A"
            )
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNav = findViewById(R.id.bottom_navigation)

        // Set start destination based on whether user is already authenticated
        val startDestination = if (authViewModel.authState.value is AuthState.Authenticated) {
            R.id.nav_dashboard
        } else {
            R.id.nav_auth
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
                authViewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Authenticated -> {
                            bottomNav.isVisible = true
                            navigateToDestination(R.id.nav_dashboard, popBackStack = true)
                        }
                        else -> {
                            bottomNav.isVisible = false
                            // If any auth state other than Authenticated, stay/go to auth fragment
                            // Note: Navigation within auth screens is handled inside AuthComposeFragment
                            navigateToDestination(R.id.nav_auth, popBackStack = true)
                        }
                    }
                }
            }
        }
    }

    private fun navigateToDestination(destinationId: Int, popBackStack: Boolean) {
        if (navController.currentDestination?.id == destinationId) return

        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .apply {
                if (popBackStack) setPopUpTo(navController.graph.startDestinationId, true)
            }
            .build()

        navController.navigate(destinationId, null, options)
    }
}
