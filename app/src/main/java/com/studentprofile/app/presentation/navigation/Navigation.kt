package com.studentprofile.app.presentation.navigation

sealed class Navigation(val route: String) {
    object Auth : Navigation("auth")
    object Dashboard : Navigation("dashboard")
}
